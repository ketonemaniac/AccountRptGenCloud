package net.ketone.accrptgen.gen;

import net.ketone.accrptgen.entity.AccountData;
import net.ketone.accrptgen.entity.Section;
import net.ketone.accrptgen.store.StorageService;
import org.apache.poi.wp.usermodel.Paragraph;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * Try http://www.javatechblog.com/java/create-header-and-footer-for-word-documents-using-docx4j/
 * https://stackoverflow.com/questions/23601516/create-docx-using-docx4j-with-multiple-headers
 * since Apache POI does not support multiple sections
 */
@Component
public class GenerationServiceApachePOI implements GenerationService {

    @Autowired
    private StorageService storageService;

    public String generate(AccountData data) {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("template.docx").getFile());

        XWPFDocument document = null;
        try {
            document = new XWPFDocument(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(XWPFHeader hdr : document.getHeaderList()) {
            XWPFParagraph para = hdr.createParagraph();
                XWPFRun run2 = para.createRun();
                run2.setText(data.getCompanyName());
                run2.setBold(true);

//            for(XWPFParagraph para : hdr.getParagraphs()) {
//                XWPFRun run2 = para.createRun();
//                run2.addCarriageReturn();
//                run2.setText(data.getCompanyName());
//                run2.setBold(true);
//            }
        }

        for(Section currSection : data.getSections()) {
            XWPFParagraph section1Pgh = null;
            for(XWPFParagraph pgh : document.getParagraphs()) {
                for(XWPFRun run : pgh.getRuns()) {
                    if(run.text().startsWith(currSection.getName())) {
                        section1Pgh = pgh;
                    }
                }

                CTPPr ctPPr = pgh.getCTP().getPPr();
                if(ctPPr != null) {

                    // Get the CTSectPr object that contains the information
                    // about the document section and strip (some of) the
                    // information from it.
                    CTSectPr sectPr = ctPPr.getSectPr();
                    // this.discoverSectionInfo(sectPr, formatter);
                }
            }
            createParagraphs(section1Pgh, currSection.getParagraphs());
            // System.out.println("[" + run.text() + "]");


        }


        String filename = data.getCompanyName() + "-" + sdf.format(data.getGenerationTime()) + ".docx";
        try {
            storageService.store(document, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filename;
    }

    void generateSectionData(XWPFDocument document, Section section) {

        
    }

    private void createParagraphs(XWPFParagraph p, List<net.ketone.accrptgen.entity.Paragraph> paragraphs) {
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
            XWPFTable table = doc.createTable();

            //create first row
            XWPFTableRow tableRowOne = table.getRow(0);
            tableRowOne.getCell(0).setText("col one, row one");
            tableRowOne.addNewTableCell().setText("col two, row one");
            tableRowOne.addNewTableCell().setText("col three, row one");

            //create second row
            XWPFTableRow tableRowTwo = table.createRow();
            tableRowTwo.getCell(0).setText("col one, row two");
            tableRowTwo.getCell(1).setText("col two, row two");
            tableRowTwo.getCell(2).setText("col three, row two");

            //create third row
            XWPFTableRow tableRowThree = table.createRow();
            tableRowThree.getCell(0).setText("col one, row three");
            tableRowThree.getCell(1).setText("col two, row three");
            tableRowThree.getCell(2).setText("col three, row three");

            XmlCursor c3 = table.getCTTbl().newCursor();
            c3.moveXml(cursor);
            c3.dispose();


            cursor.removeXml(); // Removes replacement text paragraph
            cursor.dispose();
        }
    }



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
