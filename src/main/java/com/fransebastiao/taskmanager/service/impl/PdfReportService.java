package com.fransebastiao.taskmanager.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.fransebastiao.taskmanager.dto.response.ReportData;
import com.itextpdf.io.exceptions.IOException;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PdfReportService {

    private static final String  GREEN  = "#0F6E56";
    private static final String  GRAY   = "#5F5E5A";
    private static final String  RED    = "#A32D2D";
    private static final String  LIGHT  = "#F1EFE8";
    private static final DeviceRgb COLOR_GREEN  = hex(GREEN);
    private static final DeviceRgb COLOR_GRAY   = hex(GRAY);
    private static final DeviceRgb COLOR_RED    = hex(RED);
    private static final DeviceRgb COLOR_LIGHT  = hex(LIGHT);
    private static final DeviceRgb COLOR_WHITE  = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb COLOR_BLACK  = new DeviceRgb(30,  30,  30);

    public byte[] generate(ReportData data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter   writer   = new PdfWriter(out);
        PdfDocument pdf      = new PdfDocument(writer);
        Document    document = new Document(pdf, PageSize.A4);
        document.setMargins(40, 40, 40, 40);

        addHeader(document, data);
        addSummaryMetrics(document, data);
        addTasksSection(document, data);
        addLaborSection(document, data);
        addMaterialsSection(document, data);
        addCostSummary(document, data);
        addFooter(document);

        document.close();
        log.info("PDF report generated for worker: {}", data.workerName());
        return out.toByteArray();
    }

    // -------------------------------------------------------------------------
    // Secções
    // -------------------------------------------------------------------------

    private void addHeader(Document doc, ReportData data) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        // Lado esquerdo — título e empresa
        Cell left = new Cell().setBorder(Border.NO_BORDER).setPadding(0);
        left.add(new Paragraph("Relatório Individual")
                .setFontSize(20).setBold().setFontColor(COLOR_BLACK));
        left.add(new Paragraph("CivilOps — Gestão de Obra")
                .setFontSize(10).setFontColor(COLOR_GRAY));
        header.addCell(left);

        // Lado direito — info do trabalhador
        Cell right = new Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT).setPadding(0);
        right.add(new Paragraph(data.workerName())
                .setFontSize(14).setBold().setFontColor(COLOR_BLACK));
        right.add(new Paragraph(data.workerEmail())
                .setFontSize(9).setFontColor(COLOR_GRAY));
        right.add(new Paragraph(data.roleName())
                .setFontSize(9).setFontColor(COLOR_GREEN).setBold());
        right.add(new Paragraph(String.format("Período: %s a %s",
                data.periodFrom(), data.periodTo()))
                .setFontSize(9).setFontColor(COLOR_GRAY));
        header.addCell(right);

        doc.add(header);
        doc.add(new LineSeparator(new SolidLine(0.5f))
                .setStrokeColor(COLOR_LIGHT).setMarginBottom(16));
    }

    private void addSummaryMetrics(Document doc, ReportData data) {
        doc.add(sectionTitle("Desempenho Individual"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1,1,1,1,1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        addMetricCell(table, "Total tarefas",        String.valueOf(data.totalTasks()),          null);
        addMetricCell(table, "Concluídas",            String.valueOf(data.completedTasks()),       COLOR_GREEN);
        addMetricCell(table, "Taxa de conclusão",     data.completionRate() + "%",                COLOR_GREEN);
        addMetricCell(table, "Em progresso",          String.valueOf(data.inProgressTasks()),      null);
        addMetricCell(table, "Tempo médio",           data.avgCompletionDays() + " dias",          null);

        doc.add(table);
    }

    private void addTasksSection(Document doc, ReportData data) {
        doc.add(sectionTitle("Tarefas no Período"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{3,2,1.5f,1.5f,1.5f,1.5f,1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20)
                .setFontSize(8);

        addTableHeader(table, "Tarefa", "Categoria", "Status", "Prioridade", "Prazo", "Conclusão", "%");

        data.tasks().forEach(t -> {
            table.addCell(bodyCell(t.title()));
            table.addCell(bodyCell(t.categoryName()));
            table.addCell(statusCell(t.status()));
            table.addCell(bodyCell(t.priority()));
            table.addCell(bodyCell(t.dueDate() != null ? t.dueDate().toString() : "—"));
            table.addCell(bodyCell(t.completedAt() != null ? t.completedAt().toString() : "—"));
            table.addCell(bodyCell(t.progressPercent() + "%"));
        });

        if (data.tasks().isEmpty()) {
            table.addCell(new Cell(1, 7).add(new Paragraph("Sem tarefas no período"))
                    .setFontSize(8).setFontColor(COLOR_GRAY)
                    .setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
        }

        doc.add(table);
    }

    private void addLaborSection(Document doc, ReportData data) {
        doc.add(sectionTitle("Mão de Obra — Ganhos e Penalizações"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{3,1.5f,1.5f,1.5f,1.5f,1.5f,1.5f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(12)
                .setFontSize(8);

        addTableHeader(table, "Tarefa", "Prazo (dias)", "Real (dias)", "Acordado", "Ajuste", "Final", "Tipo");

        data.laborEntries().forEach(l -> {
            table.addCell(bodyCell(l.taskTitle()));
            table.addCell(bodyCell(String.valueOf(l.allocatedDays())));
            table.addCell(bodyCell(l.actualDays() != null ? String.valueOf(l.actualDays()) : "—"));
            table.addCell(bodyCell("€" + l.agreedAmount().setScale(2, RoundingMode.HALF_UP)));
            table.addCell(adjustmentCell(l.adjustment()));
            table.addCell(bodyCell(l.finalAmount() != null ? "€" + l.finalAmount().setScale(2, RoundingMode.HALF_UP) : "—"));
            table.addCell(laborTypeCell(l.type()));
        });

        if (data.laborEntries().isEmpty()) {
            table.addCell(new Cell(1, 7).add(new Paragraph("Sem registos de mão de obra"))
                    .setFontSize(8).setFontColor(COLOR_GRAY)
                    .setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
        }

        doc.add(table);

        // Totais de mão de obra
        Table totals = new Table(UnitValue.createPercentArray(new float[]{1,1,1,1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20)
                .setFontSize(8);

        addSummaryRow(totals, "Total acordado", "€" + data.totalAgreed().setScale(2, RoundingMode.HALF_UP), null);
        addSummaryRow(totals, "Total bónus",    "€" + data.totalBonus().setScale(2, RoundingMode.HALF_UP),    COLOR_GREEN);
        addSummaryRow(totals, "Total desconto", "€" + data.totalDiscount().setScale(2, RoundingMode.HALF_UP), COLOR_RED);
        addSummaryRow(totals, "Total final",    "€" + data.totalFinal().setScale(2, RoundingMode.HALF_UP),    COLOR_GREEN);

        doc.add(totals);
    }

    private void addMaterialsSection(Document doc, ReportData data) {
        doc.add(sectionTitle("Materiais Registados"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{3,1.5f,1.5f,1.5f,1.5f,2}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20)
                .setFontSize(8);

        addTableHeader(table, "Material", "Unidade", "Quantidade", "Preço unit.", "Total", "Projecto");

        data.materials().forEach(m -> {
            table.addCell(bodyCell(m.materialName()));
            table.addCell(bodyCell(m.unit()));
            table.addCell(bodyCell(m.quantityUsed().toPlainString()));
            table.addCell(bodyCell("€" + m.unitPrice().setScale(2, RoundingMode.HALF_UP)));
            table.addCell(bodyCell("€" + m.totalCost().setScale(2, RoundingMode.HALF_UP)));
            table.addCell(bodyCell(m.projectName()));
        });

        if (data.materials().isEmpty()) {
            table.addCell(new Cell(1, 6).add(new Paragraph("Sem materiais registados"))
                    .setFontSize(8).setFontColor(COLOR_GRAY)
                    .setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
        }

        doc.add(table);
    }

    private void addCostSummary(Document doc, ReportData data) {
        doc.add(sectionTitle("Resumo de Custos"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1,1}))
                .setWidth(UnitValue.createPercentValue(50))
                .setMarginBottom(20)
                .setFontSize(9);

        addCostRow(table, "Mão de obra (final)",  data.totalFinal());
        addCostRow(table, "Materiais",             data.totalMaterialCost());
        addCostRow(table, "Total geral",           data.totalProjectCost());

        doc.add(table);
    }

    private void addFooter(Document doc) {
        doc.add(new LineSeparator(new SolidLine(0.5f))
                .setStrokeColor(COLOR_LIGHT).setMarginTop(16));
        doc.add(new Paragraph("Gerado automaticamente pelo sistema CivilOps em " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFontSize(8).setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
    }

    // -------------------------------------------------------------------------
    // Helpers de formatação
    // -------------------------------------------------------------------------

    private Paragraph sectionTitle(String title) {
        return new Paragraph(title)
                .setFontSize(11).setBold()
                .setFontColor(COLOR_BLACK)
                .setMarginBottom(8).setMarginTop(4)
                .setBorderBottom(new SolidBorder(COLOR_GREEN, 1.5f))
                .setPaddingBottom(4);
    }

    private void addMetricCell(Table table, String label, String value, DeviceRgb color) {
        Cell cell = new Cell()
                .setBackgroundColor(COLOR_LIGHT)
                .setBorder(Border.NO_BORDER)
                .setMargin(2).setPadding(8)
                .setBorderRadius(new BorderRadius(4));
        cell.add(new Paragraph(label).setFontSize(8).setFontColor(COLOR_GRAY));
        cell.add(new Paragraph(value).setFontSize(14).setBold()
                .setFontColor(color != null ? color : COLOR_BLACK));
        table.addCell(cell);
    }

    private void addTableHeader(Table table, String... headers) {
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .setBackgroundColor(COLOR_GREEN)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5)
                    .add(new Paragraph(h).setFontSize(8).setBold().setFontColor(COLOR_WHITE)));
        }
    }

    private Cell bodyCell(String text) {
        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_LIGHT, 0.5f))
                .setPadding(4)
                .add(new Paragraph(text != null ? text : "—").setFontSize(8).setFontColor(COLOR_BLACK));
    }

    private Cell statusCell(String status) {
        DeviceRgb color = switch (status) {
            case "COMPLETED"   -> COLOR_GREEN;
            case "IN_PROGRESS" -> hex("#185FA5");
            case "BLOCKED"     -> COLOR_RED;
            default            -> COLOR_GRAY;
        };
        String label = switch (status) {
            case "COMPLETED"   -> "Concluída";
            case "IN_PROGRESS" -> "Em progresso";
            case "PENDING"     -> "Pendente";
            case "BLOCKED"     -> "Bloqueada";
            default            -> status;
        };
        return new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_LIGHT, 0.5f))
                .setPadding(4)
                .add(new Paragraph(label).setFontSize(8).setFontColor(color).setBold());
    }

    private Cell adjustmentCell(BigDecimal adj) {
        if (adj == null) return bodyCell("—");
        boolean positive = adj.compareTo(BigDecimal.ZERO) >= 0;
        String text = (positive ? "+" : "") + "€" + adj.abs().setScale(2, RoundingMode.HALF_UP);
        return new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_LIGHT, 0.5f))
                .setPadding(4)
                .add(new Paragraph(text).setFontSize(8)
                        .setFontColor(positive ? COLOR_GREEN : COLOR_RED).setBold());
    }

    private Cell laborTypeCell(String type) {
        DeviceRgb color = switch (type) {
            case "BONUS"    -> COLOR_GREEN;
            case "DISCOUNT" -> COLOR_RED;
            case "ON_TIME"  -> hex("#185FA5");
            default         -> COLOR_GRAY;
        };
        String label = switch (type) {
            case "BONUS"    -> "Bónus";
            case "DISCOUNT" -> "Desconto";
            case "ON_TIME"  -> "No prazo";
            default         -> "Pendente";
        };
        return new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_LIGHT, 0.5f))
                .setPadding(4)
                .add(new Paragraph(label).setFontSize(8).setFontColor(color).setBold());
    }

    private void addSummaryRow(Table table, String label, String value, DeviceRgb color) {
        table.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_LIGHT, 0.5f))
                .setPadding(5)
                .add(new Paragraph(label).setFontSize(8).setFontColor(COLOR_GRAY)));
        table.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_LIGHT, 0.5f))
                .setPadding(5).setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(value).setFontSize(8).setBold()
                        .setFontColor(color != null ? color : COLOR_BLACK)));
    }

    private void addCostRow(Table table, String label, BigDecimal value) {
        table.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_LIGHT, 0.5f))
                .setPadding(5)
                .add(new Paragraph(label).setFontSize(9).setFontColor(COLOR_GRAY)));
        table.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_LIGHT, 0.5f))
                .setPadding(5).setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph("€" + value.setScale(2, RoundingMode.HALF_UP))
                        .setFontSize(9).setBold().setFontColor(COLOR_GREEN)));
    }

    private static DeviceRgb hex(String hex) {
        hex = hex.replace("#", "");
        return new DeviceRgb(
                Integer.parseInt(hex.substring(0,2), 16),
                Integer.parseInt(hex.substring(2,4), 16),
                Integer.parseInt(hex.substring(4,6), 16));
    }
}
