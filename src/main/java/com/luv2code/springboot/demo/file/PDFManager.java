package com.luv2code.springboot.demo.file;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfImage;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

@Service
public class PDFManager implements FileManager {
    @Override
    public String createPDF(String username,String email) {
        Document document = new Document();
        try {
            String filename = "User: " + username + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            com.itextpdf.text.Font headingFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 20);
            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 16);
            com.itextpdf.text.Font contentFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 16);


//            Paragraph title = new Paragraph("Bloop", headingFont);
//            title.setAlignment(Element.ALIGN_CENTER);
//            document.add(title);

            Image image = Image.getInstance("/Users/alex/Downloads/Bloop/src/main/resources/static/image/icon.png");
            image.setAlignment(Element.ALIGN_CENTER);
            image.scaleToFit(50f, 50f);
            document.add(image);

            Paragraph emptySpace = new Paragraph("", titleFont);
            document.add(emptySpace);

            Paragraph customerTitle = new Paragraph("Customer:", titleFont);
            document.add(customerTitle);
            Paragraph customerText = new Paragraph(username, contentFont);
            document.add(customerText);


            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            PdfPCell cell = new PdfPCell(new Paragraph("Score", titleFont));
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("Item", titleFont));
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("Quantity", titleFont));
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("Price", titleFont));
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("Total Price", titleFont));
            table.addCell(cell);

//            int itemNo = 1;
//            for (ArrayList<String> item : cart.getData().entrySet()) {
//                cell = new PdfPCell(new Paragraph(String.valueOf(itemNo), contentFont));
//                table.addCell(cell);
//                cell = new PdfPCell(new Paragraph(DBManager.getSmartphoneBrand(item.getKey().getBrandId()) + " " + item.getKey().getModel(), contentFont));
//                table.addCell(cell);
//                cell = new PdfPCell(new Paragraph(item.getValue().toString(), contentFont));
//                table.addCell(cell);
//                cell = new PdfPCell(new Paragraph(String.valueOf(item.getKey().getPrice()), contentFont));
//                table.addCell(cell);
//                cell = new PdfPCell(new Paragraph(String.valueOf(item.getKey().getPrice() * item.getValue()), contentFont));
//                table.addCell(cell);
//                itemNo++;
//            }

            document.add(table);


            Paragraph deliveryTitle = new Paragraph("Email address:", titleFont);
            document.add(deliveryTitle);
            Paragraph deliveryText = new Paragraph(email, contentFont);
            document.add(deliveryText);

            Paragraph dateTitle = new Paragraph("Registration date:", titleFont);
            document.add(dateTitle);
            Paragraph dateText = new Paragraph(LocalDateTime.now().toLocalDate().toString(), contentFont);
            document.add(dateText);


//            Paragraph totalPriceTitle = new Paragraph("Total price:", titleFont);
//            totalPriceTitle.setAlignment(Element.ALIGN_RIGHT);
//            document.add(totalPriceTitle);
//            Paragraph totalPriceText = new Paragraph(String.format("%.2f UAH", cart.getTotalPrice()), contentFont);
//            totalPriceText.setAlignment(Element.ALIGN_RIGHT);
//            document.add(totalPriceText);

            document.close();

            return filename;
        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;

    }
}
