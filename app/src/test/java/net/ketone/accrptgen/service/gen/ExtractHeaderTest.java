package net.ketone.accrptgen.service.gen;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlCursor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTInd;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
public class ExtractHeaderTest {

    @Test
    public void testExtractHeader() throws IOException, InvalidFormatException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("TKH.png").getFile());

        File template = new File(classLoader.getResource("template.docx").getFile());
        XWPFDocument templateDoc = new XWPFDocument(new FileInputStream((template)));
        List<XWPFParagraph> templateHeaderParagraphs = templateDoc.getHeaderList().stream().flatMap(
                hdr -> hdr.getParagraphs().stream()).collect(Collectors.toList());
        XWPFParagraph currPgh = templateHeaderParagraphs.stream()
                .filter(p -> p.getRuns().stream().anyMatch(run -> run.text().trim().startsWith("HeaderCover")))
                .findFirst()
                .orElseThrow();

        XWPFDocument doc = currPgh.getDocument();
        XmlCursor cursor = currPgh.getCTP().newCursor();

        CTInd indent = currPgh.getCTP().getPPr().addNewInd();
        indent.setLeft(BigInteger.valueOf(-4 * 360));

        XWPFRun newR = currPgh.createRun();
        newR.getCTR().setRPr(currPgh.getRuns().get(0).getCTR().getRPr());
        newR.addPicture(new FileInputStream((file)), XWPFDocument.PICTURE_TYPE_PNG,
                "header", Units.toEMU(450), Units.toEMU(50));

        XmlCursor c2 = currPgh.getCTP().newCursor();
        c2.moveXml(cursor);



        c2.dispose();

        File output = new File("target/extractHeaderOutput.docx");
        templateDoc.write(new FileOutputStream(output));
        templateDoc.close();
    }


}
