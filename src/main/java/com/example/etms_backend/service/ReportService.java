package com.example.etms_backend.service;

import com.example.etms_backend.entity.Task;
import com.example.etms_backend.repository.TaskRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private TaskRepository taskRepository;

    public ByteArrayInputStream generateTaskReport() {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            List<Task> tasks = taskRepository.findAll();
            PdfWriter.getInstance(document, out);
            document.open();

            Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
            font.setSize(18);
            Paragraph p = new Paragraph("ETMS - Task Report", font);
            p.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(p);
            document.add(Chunk.NEWLINE);

            // Create Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.addCell("ID");
            table.addCell("Title");
            table.addCell("Employee");
            table.addCell("Status");
            table.addCell("Priority");

            for (Task task : tasks) {
                table.addCell(String.valueOf(task.getId()));
                table.addCell(task.getTitle());
                table.addCell(task.getEmployee().getName());
                table.addCell(task.getStatus().name());
                table.addCell(task.getPriority().name());
            }

            document.add(table);
            document.close();

        } catch (DocumentException ex) {
            ex.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
