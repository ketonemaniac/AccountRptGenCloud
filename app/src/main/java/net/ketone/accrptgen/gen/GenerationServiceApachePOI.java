package net.ketone.accrptgen.gen;

import net.ketone.accrptgen.entity.*;
import net.ketone.accrptgen.store.StorageService;
import org.apache.poi.ss.formula.eval.FunctionEval;
import org.apache.poi.ss.formula.functions.DateDifFunc;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Try http://www.javatechblog.com/java/create-header-and-footer-for-word-documents-using-docx4j/
 * https://stackoverflow.com/questions/23601516/create-docx-using-docx4j-with-multiple-headers
 * since Apache POI does not support multiple sections
 */
@Component
public class GenerationServiceApachePOI implements GenerationService {

    private static final Logger logger = LoggerFactory.getLogger(GenerationServiceApachePOI.class);

    @Autowired
    private StorageService storageService;

    Map<String, XWPFParagraph> currPghs = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            FunctionEval.registerFunction("DATEDIF", new DateDifFunc());
        } catch (IllegalArgumentException e) {
            // skip error: POI already implememts DATEDIF for duplicate registers in the JVM
        }
    }


    public String generate(AccountData data) {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("template.docx").getFile());

        XWPFDocument document = null;
        try {
            document = new XWPFDocument(new FileInputStream(file));
        } catch (IOException e) {
            logger.error("Error in opening template.docx", e);
            throw new RuntimeException(e);
        }
        findParagraphLocations(document,
                data.getSections().stream().map(Section::getName).collect(Collectors.toList()));

        for(XWPFHeader hdr : document.getHeaderList()) {
            XWPFParagraph para = hdr.createParagraph();
                XWPFRun run2 = para.createRun();
                run2.setText(data.getCompanyName());
                run2.setBold(true);
        }

        for(Section currSection : data.getSections()) {

            for(SectionElement element : currSection.getElements()) {
                if(element instanceof Paragraph) {
                    write(currSection.getName(), (Paragraph) element);
                } else if(element instanceof Table) {
                    write(currSection.getName(), (Table) element);
                }
            }
            endSection(currSection.getName());
//            createParagraphs(section1Pgh, currSection.getParagraphs());
            // System.out.println("[" + run.text() + "]");
        }


        String filename = data.getCompanyName() + "-" + sdf.format(data.getGenerationTime()) + ".docx";
        try {
            storageService.store(document, filename);
        } catch (IOException e) {
            logger.error("Error storing generated file", e);
            throw new RuntimeException(e);
        }
        return filename;
    }

    /**
     * Must locate the paragraphs first,
     * otherwise will end up in ConcurrentModificationException if you try to locate them on the fly
     */
    private void findParagraphLocations(XWPFDocument document, List<String> sections) {
        for(String sectionName : sections) {
            findCurrPgh:
            for(XWPFParagraph pgh : document.getParagraphs()) {
                for(XWPFRun run : pgh.getRuns()) {
                    if(run.text().startsWith(sectionName)) {
                        currPghs.put(sectionName, pgh);
                        break findCurrPgh;
                    }
                }
//                CTPPr ctPPr = pgh.getCTP().getPPr();
//                if(ctPPr != null) {
//
//                    // Get the CTSectPr object that contains the information
//                    // about the document section and strip (some of) the
//                    // information from it.
//                    CTSectPr sectPr = ctPPr.getSectPr();
//                    // this.discoverSectionInfo(sectPr, formatter);
//                }
            }
            if(!currPghs.containsKey(sectionName)) {
                logger.warn("Cannot find paragraph " + sectionName);
            }
        }

    }


    @Override
    public void write(String sectionName, Paragraph paragraph) {
        XWPFParagraph currPgh = currPghs.get(sectionName);
        if(currPgh != null) {
            XWPFDocument doc = currPgh.getDocument();
            XmlCursor cursor = currPgh.getCTP().newCursor();

            XWPFParagraph newP = doc.createParagraph();
            newP.getCTP().setPPr(currPgh.getCTP().getPPr());
            XWPFRun newR = newP.createRun();
            newR.getCTR().setRPr(currPgh.getRuns().get(0).getCTR().getRPr());
            newR.setText(paragraph.getText());
            XmlCursor c2 = newP.getCTP().newCursor();
            c2.moveXml(cursor);
            c2.dispose();
        }
    }

    @Override
    public void write(String sectionName, Table table) {
        XWPFParagraph currPgh = currPghs.get(sectionName);
        if(currPgh != null) {
            int cols = table.getColumnWidths().size();
            int rows = table.getCells().size();

            XWPFDocument doc = currPgh.getDocument();
            XmlCursor cursor = currPgh.getCTP().newCursor();

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
                tableRowOne.getCell(i).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(table.getColumnWidths().get(i)));
            }

            for(int i=0; i < rows; i++) {
                for(int j=0; j < cols; j++) {
                    XWPFTableRow tableRow = xwpfTable.getRow(i);
                    Table.Cell cell = table.getCells().get(i).get(j);
                    tableRow.getCell(j).setText(cell.getText());

                    CTTcPr tcPr = tableRow.getCell(j).getCTTc().addNewTcPr();

                    CTTcBorders border = tcPr.addNewTcBorders();

                    border.addNewBottom().setVal(STBorder.DOUBLE);
                }
            }
            XmlCursor c3 = xwpfTable.getCTTbl().newCursor();
            c3.moveXml(cursor);
            c3.dispose();

        }
    }


    public void endSection(String sectionName) {
        XWPFParagraph currPgh = currPghs.get(sectionName);
        if(currPgh != null) {
            XmlCursor cursor = currPgh.getCTP().newCursor();
            cursor.removeXml(); // Removes replacement text paragraph
            cursor.dispose();
        }
    }
/*
    private void createParagraphs(XWPFParagraph p, List<Paragraph> paragraphs) {
        if (p != null) {
            XWPFDocument doc = p.getDocument();
            XmlCursor cursor = p.getCTP().newCursor();
            for (net.ketone.accrptgen.entity.Paragraph paragraph : paragraphs) {
                XWPFParagraph newP = doc.createParagraph();
                newP.getCTP().setPPr(p.getCTP().getPPr());
                XWPFRun newR = newP.createRun();
                newR.getCTR().setRPr(p.getRuns().get(0).getCTR().getRPr());
                newR.setText(paragraph.getText());
                XmlCursor c2 = newP.getCTP().newCursor();
                c2.moveXml(cursor);
                c2.dispose();
            }


            //create table
            XWPFTable table = doc.createTable(1, 3);

            // REMOVE ALL BORDERS
            CTTblPr tblpro = table.getCTTbl().getTblPr();
            CTTblBorders borders = tblpro.getTblBorders();
            borders.getBottom().setVal(STBorder.NONE);
            borders.getLeft().setVal(STBorder.NONE);
            borders.getRight().setVal(STBorder.NONE);
            borders.getTop().setVal(STBorder.NONE);
            //also inner borders
            borders.getInsideH().setVal(STBorder.NONE);
            borders.getInsideV().setVal(STBorder.NONE);

//            table.getCTTbl().addNewTblGrid().addNewGridCol().setW(BigInteger.valueOf(6000));
//            table.getCTTbl().getTblGrid().addNewGridCol().setW(BigInteger.valueOf(2000));

            // MS Word
            XWPFTableRow tableRowOne = table.getRow(0);
            tableRowOne.getCell(0).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(2200));
            tableRowOne.getCell(1).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(1100));
            // tableRowOne.getCell(2).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(8000));

            // OpenOffice
            CTTblWidth width = table.getCTTbl().addNewTblPr().addNewTblW();
            width.setType(STTblWidth.DXA);
            width.setW(BigInteger.valueOf(8500));

            //create first row

            tableRowOne.getCell(0).setText("1A");
            CTTcPr tcPr = tableRowOne.getCell(0).getCTTc().addNewTcPr();

            CTTcBorders border = tcPr.addNewTcBorders();

            border.addNewBottom().setVal(STBorder.DOUBLE);
            border.addNewRight().setVal(STBorder.NONE);
            border.addNewLeft().setVal(STBorder.NONE);
            border.addNewTop().setVal(STBorder.SINGLE);

            tableRowOne.getCell(1).setText("2A");
            tableRowOne.getCell(2).setText("3A");

            //create second row
            XWPFTableRow tableRowTwo = table.createRow();
            tableRowTwo.getCell(0).setText("1B");
            tableRowTwo.getCell(1).setText("2B");
            tableRowTwo.getCell(2).setText("3B");

            //create third row
            XWPFTableRow tableRowThree = table.createRow();
            tableRowThree.getCell(0).setText("1C");
            tableRowThree.getCell(1).setText("2C");
            tableRowThree.getCell(2).setText("3C");

            XmlCursor c3 = table.getCTTbl().newCursor();
            c3.moveXml(cursor);
            c3.dispose();


            // lists
            // https://stackoverflow.com/questions/44433347/apache-poi-numbered-list


            cursor.removeXml(); // Removes replacement text paragraph
            cursor.dispose();
        }
    }
*/


    private void genHeaderFooter(XWPFDocument document) {
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        XWPFHeaderFooterPolicy policy = new XWPFHeaderFooterPolicy(document, sectPr);

        //write header content
        CTP ctpHeader = CTP.Factory.newInstance();
        CTR ctrHeader = ctpHeader.addNewR();
        CTText ctHeader = ctrHeader.addNewT();
        String headerText = "This is header";
        ctHeader.setStringValue(headerText);
        XWPFParagraph headerParagraph = new XWPFParagraph(ctpHeader, document);
        XWPFParagraph[] parsHeader = new XWPFParagraph[1];
        parsHeader[0] = headerParagraph;
        policy.createHeader(XWPFHeaderFooterPolicy.DEFAULT, parsHeader);

        //write footer content
        CTP ctpFooter = CTP.Factory.newInstance();
        CTR ctrFooter = ctpFooter.addNewR();
        CTText ctFooter = ctrFooter.addNewT();
        String footerText = "This is footer";
        ctFooter.setStringValue(footerText);
        XWPFParagraph footerParagraph = new XWPFParagraph(ctpFooter, document);
        XWPFParagraph[] parsFooter = new XWPFParagraph[1];
        parsFooter[0] = footerParagraph;
        policy.createFooter(XWPFHeaderFooterPolicy.DEFAULT, parsFooter);
    }
}


/*
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);

        // The content of a paragraph needs to be wrapped in an XWPFRun object
        XWPFRun titleRun = title.createRun();
        titleRun.setText("Build Your REST API with Spring");
        titleRun.setColor("009933");
        titleRun.setBold(true);
        titleRun.setFontFamily("Courier");
        titleRun.setFontSize(20);


        XWPFParagraph subTitle = document.createParagraph();
        subTitle.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun subTitleRun = subTitle.createRun();
        subTitleRun.setText("from HTTP fundamentals to API Mastery");
        subTitleRun.setColor("00CC44");
        subTitleRun.setFontFamily("Courier");
        subTitleRun.setFontSize(16);
        subTitleRun.setTextPosition(20);
        subTitleRun.setUnderline(UnderlinePatterns.DOT_DOT_DASH);



        XWPFParagraph para1 = document.createParagraph();
        para1.setAlignment(ParagraphAlignment.BOTH);
        String string1 = "qwertyu qwerwry etyertyerty";
        XWPFRun para1Run = para1.createRun();
        para1Run.setText(string1);


        genHeaderFooter(document);

        para1Run.addCarriageReturn();                 //separate previous text from break
        para1Run.addBreak(BreakType.PAGE);
        para1Run.addBreak(BreakType.TEXT_WRAPPING);   //cancels effect of page break
        XWPFRun run2 = para1.createRun();    //create new run
        run2.setText("more text");
        run2.addCarriageReturn();
        run2.setText("one more line");

        */
