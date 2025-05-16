package com.application.savorly.service;

import com.application.savorly.domain.entity.Order;
import com.application.savorly.domain.entity.Product;
import com.application.savorly.domain.entity.Restaurant;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final float RECEIPT_WIDTH = 230f;
    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(70, 70, 70);
    private static final String SEPARATION = "----------------------------------------";

    @SneakyThrows
    public byte[] generateReceipt(com.application.savorly.domain.entity.Table table) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);

        PageSize receiptSize = new PageSize(RECEIPT_WIDTH, PageSize.A4.getHeight());
        Document document = new Document(pdf, receiptSize);
        document.setMargins(10, 10, 10, 10);

        addRestaurantDetails(document, table.getRestaurant());

        addOrderDetails(document, table);

        addProductsSummary(document, table.getOrders());

        addTotal(document, table.getCurrentCost());

        addFooter(document);

        document.close();
        return outputStream.toByteArray();
    }

    private void addRestaurantDetails(Document document, Restaurant restaurant) {
        Paragraph namePara = new Paragraph(restaurant.getName())
                .setFontSize(12)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(namePara);

        Paragraph addressPara = new Paragraph(restaurant.getAddress() + ", " + restaurant.getCity())
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(addressPara);

        Paragraph phonePara = new Paragraph("Tel: " + restaurant.getPhone())
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(phonePara);

        document.add(new Paragraph("").setFontSize(5));
        document.add(new Paragraph(SEPARATION)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("").setFontSize(5));
    }

    private void addOrderDetails(Document document, com.application.savorly.domain.entity.Table table) {
        LocalDateTime now = LocalDateTime.now();

        Paragraph receiptTitle = new Paragraph("RECEIPT")
                .setFontSize(10)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(receiptTitle);

        Table detailsTable = new Table(2);
        detailsTable.setWidth(UnitValue.createPercentValue(100));
        detailsTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
        detailsTable.setBorder(null);

        addDetailRow(detailsTable, "Date:", now.format(DATE_FORMATTER));
        addDetailRow(detailsTable, "Table:", "Table " + table.getTableNumber());

        document.add(detailsTable);
        document.add(new Paragraph("").setFontSize(5));
        document.add(new Paragraph(SEPARATION)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("").setFontSize(5));
    }

    private void addDetailRow(Table table, String label, String value) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setFontSize(8))
                .setBorder(null)
                .setTextAlignment(TextAlignment.LEFT);

        Cell valueCell = new Cell()
                .add(new Paragraph(value).setFontSize(8))
                .setBorder(null)
                .setTextAlignment(TextAlignment.RIGHT);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addProductsSummary(Document document, List<Order> orders) {
        Map<Product, Integer> productQuantities = new HashMap<>();

        for (Order order : orders) {
            for (Product product : order.getProducts()) {
                productQuantities.put(product, productQuantities.getOrDefault(product, 0) + 1);
            }
        }

        Table productsTable = new Table(3);
        productsTable.setWidth(UnitValue.createPercentValue(100));
        productsTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
        productsTable.setBorder(null);

        productsTable.addHeaderCell(createHeaderCell("Qty"));
        productsTable.addHeaderCell(createHeaderCell("Item"));
        productsTable.addHeaderCell(createHeaderCell("Price"));

        for (Map.Entry<Product, Integer> entry : productQuantities.entrySet()) {
            Product product = entry.getKey();
            Integer quantity = entry.getValue();

            Cell quantityCell = new Cell()
                    .add(new Paragraph(quantity.toString()).setFontSize(8))
                    .setBorder(null)
                    .setTextAlignment(TextAlignment.CENTER);

            Cell nameCell = new Cell()
                    .add(new Paragraph(product.getName()).setFontSize(8))
                    .setBorder(null)
                    .setTextAlignment(TextAlignment.LEFT);

            Cell priceCell = new Cell()
                    .add(new Paragraph(formatPrice(product.getPrice().multiply(new BigDecimal(quantity)))).setFontSize(8))
                    .setBorder(null)
                    .setTextAlignment(TextAlignment.RIGHT);

            productsTable.addCell(quantityCell);
            productsTable.addCell(nameCell);
            productsTable.addCell(priceCell);
        }

        document.add(productsTable);
        document.add(new Paragraph("").setFontSize(5));
    }

    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(8).setBold())
                .setBackgroundColor(HEADER_COLOR)
                .setFontColor(ColorConstants.WHITE)
                .setBorder(null)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private void addTotal(Document document, BigDecimal total) {
        document.add(new Paragraph(SEPARATION)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER));

        Table totalTable = new Table(2);
        totalTable.setWidth(UnitValue.createPercentValue(100));
        totalTable.setBorder(null);

        Cell totalLabelCell = new Cell()
                .add(new Paragraph("TOTAL").setFontSize(10).setBold())
                .setBorder(null)
                .setTextAlignment(TextAlignment.LEFT);

        Cell totalValueCell = new Cell()
                .add(new Paragraph(formatPrice(total)).setFontSize(10).setBold())
                .setBorder(null)
                .setTextAlignment(TextAlignment.RIGHT);

        totalTable.addCell(totalLabelCell);
        totalTable.addCell(totalValueCell);

        document.add(totalTable);
        document.add(new Paragraph("").setFontSize(5));
    }

    private void addFooter(Document document) {
        document.add(new Paragraph(SEPARATION)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Thank you for your visit!")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Please come again soon")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private String formatPrice(BigDecimal price) {
        return String.format("%.2f", price);
    }
}
