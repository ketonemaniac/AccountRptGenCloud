package net.ketone.accrptgen.gen;

import net.ketone.accrptgen.store.StorageService;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

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
            for(XWPFParagraph para : hdr.getParagraphs()) {
                for(XWPFRun run : para.getRuns()) {
                    System.out.println(run.text());
                }
                XWPFRun run2 = para.createRun();
                run2.addCarriageReturn();
                run2.setText(data.companyName);
                run2.setBold(true);
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
        String filename = data.companyName + "-" + sdf.format(data.generationTime) + ".docx";
        try {
            storageService.store(document, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filename;
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
