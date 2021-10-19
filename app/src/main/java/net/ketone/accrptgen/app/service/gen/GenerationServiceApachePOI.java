package net.ketone.accrptgen.app.service.gen;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.app.domain.gen.*;
import net.ketone.accrptgen.common.credentials.SettingsService;
import net.ketone.accrptgen.common.store.StorageService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.eval.FunctionEval;
import org.apache.poi.ss.formula.functions.DateDifFunc;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.ketone.accrptgen.common.constants.Constants.TEMPLATE_FILE;

/**
 * Try http://www.javatechblog.com/java/create-header-and-footer-for-word-documents-using-docx4j/
 * https://stackoverflow.com/questions/23601516/create-docx-using-docx4j-with-multiple-headers
 * since Apache POI does not support multiple sections
 */
@Slf4j
@Component
@Scope(value = "prototype")
public class GenerationServiceApachePOI implements GenerationService {

    // this converts cm to "twips" used internally for specifying height
    private static final int twipsPerCm =  566;
    // this converts the user input height to cm
    private static final int twipsPerInput = (int) (0.035 * twipsPerCm);

    private static final int twipsPerIndent = 360;

    private final StorageService persistentStorage;

    private final SettingsService configurationService;


    private Map<String, XWPFParagraph> currPghs = new HashMap<>();

    private Map<String, XWPFParagraph> pghHeaders = new HashMap<>();

    private Map<String, XWPFParagraph> pghFooters = new HashMap<>();

    private Map<Integer, BigInteger> numberedLists = new HashMap<>();

    private Map<String, String> bannerMap;

    public GenerationServiceApachePOI(final StorageService persistentStorage,
                                      final SettingsService configurationService) {
        this.persistentStorage = persistentStorage;
        this.configurationService = configurationService;
        try {
            FunctionEval.registerFunction("DATEDIF", new DateDifFunc());
        } catch (IllegalArgumentException e) {
            // skip error: POI already implememts DATEDIF for duplicate registers in the JVM
        }
        bannerMap = getCredentialsMap("auditor.");
    }

    private Map<String, String> getCredentialsMap(final String prefix) {
        return configurationService.getSettings().entrySet().stream()
                .filter(entry -> entry.getKey().toString().startsWith(prefix))
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(entry -> entry.getKey().toString().split("\\.")[1]),
                        map -> map.entrySet().stream()
                                .collect(Collectors.toMap(entry -> entry.getValue()
                                                .stream()
                                                .filter(e -> e.getKey().toString().contains(".name"))
                                                .findFirst()
                                                .get().getValue().toString(),
                                        entry -> entry.getValue()
                                                .stream()
                                                .filter(e -> e.getKey().toString().contains(".banner"))
                                                .findFirst()
                                                .get().getValue().toString())
                                ))
                );
    }


    /**
     * The trick here is to make a template docx with only the words "SectionX" and locate them.
     * Then we REPLACE the contents of each section by corresponding data
     * There you do not need to deal with the creation of multi-section documents
     * which Apache POI seems to struggle in implementing
     * @param data
     * @return the filename of the generated docx
     */
    public byte[] generate(AccountData data) throws IOException {

        XWPFDocument document = null;
        try {
            document = new XWPFDocument(persistentStorage.loadAsInputStream(TEMPLATE_FILE));
        } catch (IOException e) {
            log.error("Error in opening " + TEMPLATE_FILE, e);
            throw new RuntimeException(e);
        }
        List<XWPFParagraph> contentParagraphs = document.getParagraphs();
        currPghs = findParagraphLocations(data.getSections().stream().map(Section::getName).collect(Collectors.toList())
                , contentParagraphs);
        List<XWPFParagraph> headerParagraphs = document.getHeaderList().stream().flatMap(
                                hdr -> hdr.getParagraphs().stream()).collect(Collectors.toList());
        pghHeaders = findParagraphLocations(data.getSections().stream().map(section -> "Header" + section.getName())
                                                .collect(Collectors.toList()), headerParagraphs);
        List<XWPFParagraph> footerParagraphs = document.getFooterList().stream().flatMap(
                hdr -> hdr.getParagraphs().stream()).collect(Collectors.toList());
        pghFooters = findParagraphLocations(data.getSections().stream().map(section -> "Footer" + section.getName())
                .collect(Collectors.toList()), footerParagraphs);


        for(Section currSection : data.getSections()) {

            for(SectionElement element : currSection.getElements()) {
                if(element instanceof Header) {
                    write(currSection.getName(), (Header) element, data.getCompanyName());
                } else if(element instanceof Paragraph) {
                    write(currSection.getName(), (Paragraph) element);
                } else if(element instanceof Table) {
                    write(currSection.getName(), (Table) element);
                }
                clearNumeberedList();
            }
            endSection(currPghs.get(currSection.getName()));
            endSection(pghHeaders.get("Header" + currSection.getName()));
            endSection(pghFooters.get("Footer" + currSection.getName()));
        }
        // update TOC
        document.enforceUpdateFields();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        document.write(os);
        document.close();
        byte[] bytes = os.toByteArray();
        os.close();
        return bytes;
    }

    /**
     * Must locate the paragraphs first,
     * otherwise will end up in ConcurrentModificationException if you try to locate them on the fly
     */
    private Map<String, XWPFParagraph> findParagraphLocations(List<String> sections, List<XWPFParagraph> paragraphs) {
        Map<String, XWPFParagraph> pghsMap = new HashMap<>();
        for(String sectionName : sections) {
            findCurrPgh:
            for(XWPFParagraph pgh : paragraphs) {
                for(XWPFRun run : pgh.getRuns()) {
                    if(run.text().trim().equalsIgnoreCase(sectionName)) {
                        pghsMap.put(sectionName, pgh);
//                        break findCurrPgh;
                    } else if(run.text().trim().equalsIgnoreCase(sectionName + "Auditor")) {
                        pghsMap.put(sectionName + "Auditor", pgh);
                    }
                }
            }
            if(!pghsMap.containsKey(sectionName)) {
                log.warn("Cannot find paragraph " + sectionName);
            }
        }
        return pghsMap;
    }

    @Override
    public void write(String sectionName, Paragraph paragraph) {
        XWPFParagraph currPgh = currPghs.get(sectionName);
        doWrite(currPgh, paragraph, null);
    }

    /**
     * Places the runContent directly in the run that is passed in
     * @param currPgh
     * @param runContent
     */
    private void doWriteRun(XWPFParagraph currPgh, Consumer<XWPFRun> runContent) {
        if(currPgh != null) {
            XWPFDocument doc = currPgh.getDocument();
            XmlCursor cursor = currPgh.getCTP().newCursor();
            XWPFRun newR = currPgh.getRuns().get(0);
            newR.getCTR().removeT(0);
            newR.setText(StringUtils.EMPTY);
            runContent.accept(newR);
            XmlCursor c2 = currPgh.getCTP().newCursor();
            c2.moveXml(cursor);
            c2.dispose();
        }
    }


    private void doWrite(XWPFParagraph currPgh, Paragraph paragraph, Consumer<XWPFParagraph> formatting) {
        if(currPgh != null) {
            XWPFDocument doc = currPgh.getDocument();
            XmlCursor cursor = currPgh.getCTP().newCursor();

            XWPFParagraph newP = doc.createParagraph();
            newP.getCTP().setPPr(currPgh.getCTP().getPPr());
            XWPFRun newR = newP.createRun();
            newR.getCTR().setRPr(currPgh.getRuns().get(0).getCTR().getRPr());
            newR.setBold(paragraph.isBold());

            if(paragraph.getIndent() > 0) {
                CTInd ind = newP.getCTP().getPPr().addNewInd();
                if(paragraph.isItem()) {
                    ind.setLeft(BigInteger.valueOf((paragraph.getIndent()+1) * twipsPerIndent));
                } else {
                    ind.setLeft(BigInteger.valueOf((paragraph.getIndent()) * twipsPerIndent));
                }
            }
            if(paragraph.isItem()) {
                newP.setNumID(getNumberedList(doc, paragraph.getIndent()));
            }

            newR.setText(paragraph.getText());
            if(formatting != null) {
                formatting.accept(newP);
            }
            XmlCursor c2 = newP.getCTP().newCursor();
            c2.moveXml(cursor);
            c2.dispose();
        }
    }


    public void write(String sectionName, Header header, String companyName) {
        XWPFParagraph currPgh = pghHeaders.get("Header" + sectionName);
        if(StringUtils.isNotEmpty(header.getAuditorName())) {
            String banner = Optional.ofNullable(header.getAuditorName().trim())
                        .map(s -> s.split(" "))
                        .map(arr -> arr[0])
                        .map(bannerMap::get)
                    .orElseThrow(() -> new RuntimeException("Unable to find banner from Auditor name" +
                            header.getAuditorName()));
            XWPFParagraph currAuditorPgh = pghHeaders.get("Header" + sectionName + "Auditor");
            doWriteRun(currAuditorPgh, newR -> {
                Try.run(() -> {
                    newR.addPicture(persistentStorage.loadAsInputStream(
                            StorageService.BANNER_PATH + banner),
                            XWPFDocument.PICTURE_TYPE_PNG,
                            header.getAuditorName().trim(),
                            Units.toEMU(550), Units.toEMU(37));
                    CTInd indent = newR.getParagraph().getCTP().getPPr().addNewInd();
                    indent.setLeft(BigInteger.valueOf(-3 * twipsPerIndent));
                }).get();
            });
            // one more empty line
            Paragraph p = new Paragraph();
            p.setText("");
            doWrite(currPgh, p, null);
            return;
        }
        if(header.getAuditorAddress() != null) {
            currPgh = pghFooters.get("Footer" + sectionName);
            header.setText(header.getAuditorAddress());
            doWrite(currPgh, header, null);
            return;
        }
        if(header.isFirstLine() && header.isHasCompanyName()) {
            Paragraph p = new Paragraph();
            p.setText(companyName);
            p.setBold(true);
            doWrite(currPgh, p, null);
        }
        if(header.isLastLine()) {
            if(header.getUnderline().equals(Header.Underline.BEFORE_LAST))
                doWrite(currPgh, header, p -> p.setBorderTop(Borders.SINGLE));
            else if(header.getUnderline().equals(Header.Underline.AFTER_LAST))
                doWrite(currPgh, header, p -> p.setBorderBottom(Borders.SINGLE));
            else
                doWrite(currPgh, header, null);
        } else {
            doWrite(currPgh, header, null);
        }



    }

    @Override
    public void write(String sectionName, Table table) {
        XWPFParagraph currPgh = currPghs.get(sectionName);
        if(currPgh != null && table.getCells() != null) {   // skip empty table
            int cols = table.getColumnWidths().size();
            int rows = table.getCells().size();

            XWPFDocument doc = currPgh.getDocument();
            XmlCursor cursor = currPgh.getCTP().newCursor();
            setSingleLineSpacing(currPgh);

            //create table
            XWPFTable xwpfTable = doc.createTable(rows, cols);

            // REMOVE ALL BORDERS
            CTTblPr tblpro = xwpfTable.getCTTbl().getTblPr();
            CTTblBorders borders = tblpro.getTblBorders();
            borders.getBottom().setVal(STBorder.NONE);
            borders.getLeft().setVal(STBorder.NONE);
            borders.getRight().setVal(STBorder.NONE);
            borders.getTop().setVal(STBorder.NONE);
            //also inner borders
            borders.getInsideH().setVal(STBorder.NONE);
            borders.getInsideV().setVal(STBorder.NONE);

            // column widths
            // MS Word
            XWPFTableRow tableRowOne = xwpfTable.getRow(0);
            for(int i=0; i < table.getColumnWidths().size(); i++) {
                tableRowOne.getCell(i).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(table.getColumnWidths().get(i) * twipsPerInput));
            }

            for(int i=0; i < rows; i++) {
                XWPFTableRow tableRow = xwpfTable.getRow(i);
                tableRow.setHeight((int)(twipsPerInput * table.getRowHeight())); //set height 1/10 inch.

                for(int j=0; j < cols; j++) {
                    try {

                        Table.Cell cell = table.getCells().get(i).get(j);
                        // tableRow.getCell(j).setText(cell.getText());
                        tableRow.getCell(j).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                        XWPFParagraph paragraph = tableRow.getCell(j).getParagraphArray(0);
                        paragraph.createRun().setText(cell.getText());
                        setSingleLineSpacing(paragraph);
                        paragraph.getRuns().get(0).setBold(cell.isBold());
                        paragraph.getRuns().get(0).setUnderline(cell.isUnderline() ? UnderlinePatterns.SINGLE : UnderlinePatterns.NONE);

                        CTTcPr tcPr = tableRow.getCell(j).getCTTc().addNewTcPr();
                        CTTcBorders border = tcPr.addNewTcBorders();
                        if(cell.getBottomBorderStyle() != null) {
                            switch(cell.getBottomBorderStyle()) {
                                case SINGLE_LINE:
                                    CTBorder bottomBorder = border.addNewBottom();
                                    bottomBorder.setVal(STBorder.SINGLE);
                                    bottomBorder.setSz(BigInteger.valueOf(6));
                                    break;
                                case DOUBLE_LINE:
                                    bottomBorder = border.addNewBottom();
                                    bottomBorder.setVal(STBorder.DOUBLE);
                                    bottomBorder.setSz(BigInteger.valueOf(6));
                                    break;
                            }
                        }
                        if(cell.getAlignment() != null) {
                            switch (cell.getAlignment()) {
                                case LEFT:
                                    paragraph.setAlignment(ParagraphAlignment.LEFT); break;
                                case CENTER:
                                    paragraph.setAlignment(ParagraphAlignment.CENTER); break;
                                case RIGHT:
                                    paragraph.setAlignment(ParagraphAlignment.RIGHT); break;
                            }
                        }
                    }catch (Exception e) {
                        log.error("write table error: " + sectionName + " i:" + i + " j:" + j, e);
                    }
                }
            }
            XmlCursor c3 = xwpfTable.getCTTbl().newCursor();
            c3.moveXml(cursor);
            c3.dispose();

        }
    }


    public void endSection(XWPFParagraph currPgh) {
        if(currPgh != null) {
            XmlCursor cursor = currPgh.getCTP().newCursor();
            cursor.removeXml(); // Removes replacement text paragraph
            cursor.dispose();
        }
    }


    private void setSingleLineSpacing(XWPFParagraph para) {
        CTPPr ppr = para.getCTP().getPPr();
        if (ppr == null) ppr = para.getCTP().addNewPPr();
        CTSpacing spacing = ppr.isSetSpacing()? ppr.getSpacing() : ppr.addNewSpacing();
        spacing.setAfter(BigInteger.valueOf(0));
        spacing.setBefore(BigInteger.valueOf(0));
        spacing.setLineRule(STLineSpacingRule.AUTO);
        spacing.setLine(BigInteger.valueOf(240));
    }

    private BigInteger getNumberedList(XWPFDocument document, int level) {

        if(numberedLists.get(level) != null)
            return numberedLists.get(level);

        CTAbstractNum cTAbstractNum = CTAbstractNum.Factory.newInstance();
        //Next we set the AbstractNumId. This requires care.
        //Since we are in a new document we can start numbering from 0.
        //But if we have an existing document, we must determine the next free number first.
        cTAbstractNum.setAbstractNumId(BigInteger.valueOf(level));

        // Decimal list
        CTLvl cTLvl = cTAbstractNum.addNewLvl();

        // requirements
        switch (level) {
            case 0:
                cTLvl.addNewNumFmt().setVal(STNumberFormat.DECIMAL);
                cTLvl.addNewRPr().addNewB();
                break;
            case 1:
                cTLvl.addNewNumFmt().setVal(STNumberFormat.LOWER_LETTER);
                break;
            case 2:
                cTLvl.addNewNumFmt().setVal(STNumberFormat.LOWER_ROMAN);
                break;
        }
        cTLvl.addNewLvlText().setVal("%1.");
        cTLvl.addNewStart().setVal(BigInteger.valueOf(1));
        CTPPr ctpPr = cTLvl.addNewPPr();
        CTInd ctInd = ctpPr.addNewInd();
        // magic indentations...
        // left controls the number itself
        ctInd.setLeft(BigInteger.valueOf(twipsPerIndent));   // if I do not set it becomes -0.25cm
        // hanging controls the numbers after it
        ctInd.setHanging(BigInteger.valueOf(twipsPerIndent));

        XWPFAbstractNum abstractNum = new XWPFAbstractNum(cTAbstractNum);

        XWPFNumbering numbering = document.createNumbering();

        BigInteger abstractNumID = numbering.addAbstractNum(abstractNum);

        BigInteger numID = numbering.addNum(abstractNumID);
        numberedLists.put(level, numID);
        return numID;
    }

    /**
     * Clear list at the end of every section
     */
    private void clearNumeberedList() {
        numberedLists.clear();
    }

}
