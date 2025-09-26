package com.shopmanager.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.shopmanager.model.Sale;
import com.shopmanager.model.SaleItem;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ReportService {
    public File generateInvoice(Sale sale, File targetFile) throws IOException {
        try (PdfWriter writer = new PdfWriter(targetFile);
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf, PageSize.A4)) {

            doc.add(new Paragraph("Facture").setFontSize(20).setBold());
            doc.add(new Paragraph("Date: " + sale.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            String customerName = sale.getCustomer()!=null ? sale.getCustomer().getName() : "Client comptoir";
            doc.add(new Paragraph("Client: " + customerName));
            doc.add(new Paragraph(" "));

            float[] cols = {140, 60, 60, 80};
            Table table = new Table(cols).useAllAvailableWidth();
            table.addHeaderCell(header("Produit"));
            table.addHeaderCell(header("Qté"));
            table.addHeaderCell(header("PU"));
            table.addHeaderCell(header("Total"));

            for (SaleItem it : sale.getItems()) {
                table.addCell(cell(text(it.getProduct().getName())));
                table.addCell(cell(text(String.valueOf(it.getQuantity()))));
                table.addCell(cell(text(String.format("%.2f MRU", it.getUnitPrice()))));
                table.addCell(cell(text(String.format("%.2f MRU", it.getUnitPrice() * it.getQuantity()))));
            }

            doc.add(table);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("TVA: " + String.format("%.2f MRU", sale.getVat())));
            doc.add(new Paragraph("Total TTC: " + String.format("%.2f MRU", sale.getTotal()))
                    .setBold().setFontColor(ColorConstants.BLACK));
        }
        return targetFile;
    }

    private Paragraph text(String s) { return new Paragraph(s).setFontSize(10); }
    private Cell header(String s) { return new Cell().add(new Paragraph(s).setBold()).setBorder(Border.NO_BORDER); }
    private Cell cell(Paragraph p) { return new Cell().add(p).setBorder(Border.NO_BORDER); }
}
