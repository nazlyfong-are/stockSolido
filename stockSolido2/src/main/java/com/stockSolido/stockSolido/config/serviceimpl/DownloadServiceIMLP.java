package com.stockSolido.stockSolido.config.serviceimpl;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.stockSolido.stockSolido.service.downloadService;
import com.stockSolido.stockSolido.repository.DownloadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class DownloadServiceIMLP implements downloadService {

    // PALETA DE COLORES
    private static final Color COLOR_ROJO        = new Color(0xC0, 0x39, 0x2B);
    private static final Color COLOR_NARANJA     = new Color(0xE6, 0x7E, 0x22);
    private static final Color COLOR_VERDE       = new Color(0x27, 0xAE, 0x60);
    private static final Color COLOR_GRIS        = new Color(0x63, 0x6E, 0x72);
    private static final Color COLOR_HEADER_BG   = new Color(0x2C, 0x3E, 0x50);
    private static final Color COLOR_HEADER_TEXT = Color.WHITE;
    private static final Color COLOR_ROW_ALT     = new Color(0xF8, 0xF9, 0xFA);
    private static final Color COLOR_BORDE       = new Color(0xDE, 0xE2, 0xE6);
 
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
 
    @Autowired
    private DownloadRepository downloadRepository;
 
    //HISTORIAL COMPLETO
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public byte[] generarHistorialCompleto() throws Exception {
        List<Map<String, Object>> datos = downloadRepository.findHistorialCompleto();
        String[] cabeceras = {"#", "Cliente", "Tipo de servicio", "Fecha", "Estado", "Total"};
        int[]    anchos    = {4, 22, 25, 13, 13, 13};
 
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = crearDocumento();
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            agregarPieDePagina(writer);
            doc.open();
            agregarEncabezado(doc, "Historial Completo de Servicios", COLOR_ROJO);
            agregarMetadatos(doc, "Todos los servicios finalizados registrados en el sistema.");
            PdfPTable tabla = crearTabla(cabeceras, anchos, COLOR_ROJO);
            int i = 1;
            for (Map<String, Object> fila : datos) {
                boolean par = (i % 2 == 0);
                agregarCelda(tabla, String.valueOf(i++),        par, Element.ALIGN_CENTER);
                agregarCelda(tabla, str(fila, "cliente"),       par, Element.ALIGN_LEFT);
                agregarCelda(tabla, str(fila, "tipo_servicio"), par, Element.ALIGN_LEFT);
                agregarCelda(tabla, fecha(fila, "fecha"),       par, Element.ALIGN_CENTER);
                agregarCelda(tabla, str(fila, "estado"),        par, Element.ALIGN_CENTER);
                agregarCelda(tabla, moneda(fila, "total"),      par, Element.ALIGN_RIGHT);
            }
            doc.add(tabla);
            agregarTotalizador(doc, datos, "total");
            doc.close();
            return out.toByteArray();
        }
    }
 
    // SOLICITUDES X ESTADO
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public byte[] generarPorEstado(String estado) throws Exception {
        List<Map<String, Object>> datos = downloadRepository.findPorEstado(estado);
        String titulo = (estado != null && !estado.isBlank())
                ? "Solicitudes – Estado: " + estado
                : "Solicitudes – Todos los estados";
        String[] cabeceras = {"#", "Cliente", "Tipo de servicio", "Fecha", "Estado", "Total"};
        int[]    anchos    = {4, 22, 25, 13, 13, 13};
 
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = crearDocumento();
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            agregarPieDePagina(writer);
            doc.open();
            agregarEncabezado(doc, titulo, COLOR_ROJO);
            agregarMetadatos(doc, "Solicitudes agrupadas por estado de servicio.");
            PdfPTable tabla = crearTabla(cabeceras, anchos, COLOR_ROJO);
            int i = 1;
            for (Map<String, Object> fila : datos) {
                boolean par = (i % 2 == 0);
                agregarCelda(tabla, String.valueOf(i++),        par, Element.ALIGN_CENTER);
                agregarCelda(tabla, str(fila, "cliente"),       par, Element.ALIGN_LEFT);
                agregarCelda(tabla, str(fila, "tipo_servicio"), par, Element.ALIGN_LEFT);
                agregarCelda(tabla, fecha(fila, "fecha"),       par, Element.ALIGN_CENTER);
                agregarCelda(tabla, str(fila, "estado"),        par, Element.ALIGN_CENTER);
                agregarCelda(tabla, moneda(fila, "total"),      par, Element.ALIGN_RIGHT);
            }
            doc.add(tabla);
            doc.close();
            return out.toByteArray();
        }
    }
 
    // INGRESOS X FECHAS
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public byte[] generarIngresosPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin) throws Exception {
        List<Map<String, Object>> datos = downloadRepository.findIngresosPorPeriodo(fechaInicio, fechaFin);
        String rango = construirRango(fechaInicio, fechaFin);
        String[] cabeceras = {"#", "Cliente", "Tipo de servicio", "Fecha", "Total"};
        int[]    anchos    = {5, 28, 30, 17, 20};
 
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = crearDocumento();
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            agregarPieDePagina(writer);
            doc.open();
            agregarEncabezado(doc, "Ingresos por Período", COLOR_NARANJA);
            agregarMetadatos(doc, "Período: " + rango);
            PdfPTable tabla = crearTabla(cabeceras, anchos, COLOR_NARANJA);
            int i = 1;
            for (Map<String, Object> fila : datos) {
                boolean par = (i % 2 == 0);
                agregarCelda(tabla, String.valueOf(i++),        par, Element.ALIGN_CENTER);
                agregarCelda(tabla, str(fila, "cliente"),       par, Element.ALIGN_LEFT);
                agregarCelda(tabla, str(fila, "tipo_servicio"), par, Element.ALIGN_LEFT);
                agregarCelda(tabla, fecha(fila, "fecha"),       par, Element.ALIGN_CENTER);
                agregarCelda(tabla, moneda(fila, "total"),      par, Element.ALIGN_RIGHT);
            }
            doc.add(tabla);
            agregarTotalizador(doc, datos, "total");
            doc.close();
            return out.toByteArray();
        }
    }
 
    // TOP DE SERVICIOS
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public byte[] generarRankingServicios() throws Exception {
        List<Map<String, Object>> datos = downloadRepository.findRankingServicios();
        String[] cabeceras = {"Posición", "Tipo de servicio", "Cantidad de solicitudes"};
        int[]    anchos    = {15, 55, 30};
 
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = crearDocumento();
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            agregarPieDePagina(writer);
            doc.open();
            agregarEncabezado(doc, "Servicios Más Solicitados", COLOR_VERDE);
            agregarMetadatos(doc, "Ranking de tipos de servicio ordenados por volumen de solicitudes.");
            PdfPTable tabla = crearTabla(cabeceras, anchos, COLOR_VERDE);
            int i = 1;
            for (Map<String, Object> fila : datos) {
                boolean par = (i % 2 == 0);
                agregarCelda(tabla, "#" + i++,                  par, Element.ALIGN_CENTER);
                agregarCelda(tabla, str(fila, "tipo_servicio"), par, Element.ALIGN_LEFT);
                agregarCelda(tabla, str(fila, "cantidad"),      par, Element.ALIGN_CENTER);
            }
            doc.add(tabla);
            doc.close();
            return out.toByteArray();
        }
    }
 
    //CLIENTES REGISTRADOS
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public byte[] generarClientesRegistrados() throws Exception {
        List<Map<String, Object>> datos = downloadRepository.findClientesRegistrados();
        String[] cabeceras = {"#", "Nombre", "Tipo", "Documento", "Teléfono", "Correo"};
        int[]    anchos    = {4, 26, 10, 14, 14, 22};
 
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = crearDocumento();
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            agregarPieDePagina(writer);
            doc.open();
            agregarEncabezado(doc, "Clientes Registrados", COLOR_GRIS);
            agregarMetadatos(doc, "Listado completo de clientes activos en el sistema.");
            PdfPTable tabla = crearTabla(cabeceras, anchos, COLOR_GRIS);
            int i = 1;
            for (Map<String, Object> fila : datos) {
                boolean par = (i % 2 == 0);
                agregarCelda(tabla, String.valueOf(i++),    par, Element.ALIGN_CENTER);
                agregarCelda(tabla, str(fila, "nombre"),    par, Element.ALIGN_LEFT);
                agregarCelda(tabla, str(fila, "tipo"),      par, Element.ALIGN_CENTER);
                agregarCelda(tabla, str(fila, "documento"), par, Element.ALIGN_CENTER);
                agregarCelda(tabla, str(fila, "telefono"),  par, Element.ALIGN_CENTER);
                agregarCelda(tabla, str(fila, "correo"),    par, Element.ALIGN_LEFT);
            }
            doc.add(tabla);
            doc.close();
            return out.toByteArray();
        }
    }
 
    //PROXIMOS SERVICIOS
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public byte[] generarProximosServicios() throws Exception {
        List<Map<String, Object>> datos = downloadRepository.findProximosServicios();
        String[] cabeceras = {"#", "Cliente", "Tipo de servicio", "Fecha programada", "Estado"};
        int[]    anchos    = {5, 30, 30, 20, 15};
 
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = crearDocumento();
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            agregarPieDePagina(writer);
            doc.open();
            agregarEncabezado(doc, "Próximos Servicios Programados", COLOR_NARANJA);
            agregarMetadatos(doc, "Solicitudes con fecha futura, ordenadas por proximidad.");
            PdfPTable tabla = crearTabla(cabeceras, anchos, COLOR_NARANJA);
            int i = 1;
            for (Map<String, Object> fila : datos) {
                boolean par = (i % 2 == 0);
                agregarCelda(tabla, String.valueOf(i++),        par, Element.ALIGN_CENTER);
                agregarCelda(tabla, str(fila, "cliente"),       par, Element.ALIGN_LEFT);
                agregarCelda(tabla, str(fila, "tipo_servicio"), par, Element.ALIGN_LEFT);
                agregarCelda(tabla, fecha(fila, "fecha"),       par, Element.ALIGN_CENTER);
                agregarCelda(tabla, str(fila, "estado"),        par, Element.ALIGN_CENTER);
            }
            doc.add(tabla);
            doc.close();
            return out.toByteArray();
        }
    }
 
    // SERVICIOS PENDIENTES
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public byte[] generarServiciosPendientes() throws Exception {
        List<Map<String, Object>> datos = downloadRepository.findServiciosPendientes();
        String[] cabeceras = {"#", "Cliente", "Tipo de servicio", "Fecha", "Estado"};
        int[]    anchos    = {5, 30, 30, 18, 17};
 
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = crearDocumento();
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            agregarPieDePagina(writer);
            doc.open();
            agregarEncabezado(doc, "Servicios Pendientes", COLOR_ROJO);
            agregarMetadatos(doc, "Solicitudes en espera o en proceso pendientes de finalizar.");
            PdfPTable tabla = crearTabla(cabeceras, anchos, COLOR_ROJO);
            int i = 1;
            for (Map<String, Object> fila : datos) {
                boolean par = (i % 2 == 0);
                agregarCelda(tabla, String.valueOf(i++),        par, Element.ALIGN_CENTER);
                agregarCelda(tabla, str(fila, "cliente"),       par, Element.ALIGN_LEFT);
                agregarCelda(tabla, str(fila, "tipo_servicio"), par, Element.ALIGN_LEFT);
                agregarCelda(tabla, fecha(fila, "fecha"),       par, Element.ALIGN_CENTER);
                agregarCelda(tabla, str(fila, "estado"),        par, Element.ALIGN_CENTER);
            }
            doc.add(tabla);
            doc.close();
            return out.toByteArray();
        }
    }
 
    // REPORTE POR CLIENTE
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public byte[] generarReportePorCliente(String documento) throws Exception {
        Map<String, Object> infoCliente = downloadRepository.findInfoCliente(documento);
        List<Map<String, Object>> datos = downloadRepository.findSolicitudesPorCliente(documento);
        String nombreCliente = infoCliente != null ? str(infoCliente, "nombre") : documento;
        String[] cabeceras = {"#", "Tipo de servicio", "Fecha", "Estado", "Total"};
        int[]    anchos    = {5, 40, 18, 18, 19};
 
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = crearDocumento();
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            agregarPieDePagina(writer);
            doc.open();
            agregarEncabezado(doc, "Reporte por Cliente", COLOR_VERDE);
            agregarMetadatos(doc, "Cliente: " + nombreCliente + "  |  Documento: " + documento);
            if (infoCliente != null) agregarFichaCliente(doc, infoCliente);
            PdfPTable tabla = crearTabla(cabeceras, anchos, COLOR_VERDE);
            int i = 1;
            for (Map<String, Object> fila : datos) {
                boolean par = (i % 2 == 0);
                agregarCelda(tabla, String.valueOf(i++),        par, Element.ALIGN_CENTER);
                agregarCelda(tabla, str(fila, "tipo_servicio"), par, Element.ALIGN_LEFT);
                agregarCelda(tabla, fecha(fila, "fecha"),       par, Element.ALIGN_CENTER);
                agregarCelda(tabla, str(fila, "estado"),        par, Element.ALIGN_CENTER);
                agregarCelda(tabla, moneda(fila, "total"),      par, Element.ALIGN_RIGHT);
            }
            doc.add(tabla);
            agregarTotalizador(doc, datos, "total");
            doc.close();
            return out.toByteArray();
        }
    }
 
    // HELPERS PARA CONTRUCCION DEL PDF
    @PreAuthorize("hasRole('ADMIN')")
    private Document crearDocumento() {
        return new Document(PageSize.A4.rotate(), 36, 36, 60, 50);
    }
 
    private void agregarEncabezado(Document doc, String titulo, Color acento) throws Exception {
        try {
            java.net.URL logoUrl = getClass().getResource("/static/img/logo.png");
            if (logoUrl != null) {
                Image logo = Image.getInstance(logoUrl);
                logo.scaleToFit(90, 40);
                logo.setAlignment(Element.ALIGN_LEFT);
                doc.add(logo);
            }
        } catch (Exception ignored) {}
 
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.setSpacingBefore(6);
        header.setSpacingAfter(12);
 
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(acento);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(10);
 
        Font fontTitulo = new Font(Font.HELVETICA, 16, Font.BOLD, COLOR_HEADER_TEXT);
        Paragraph parrafo = new Paragraph(titulo, fontTitulo);
        parrafo.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(parrafo);
 
        Font fontSub = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.WHITE);
        Paragraph sub = new Paragraph("Generado el " + LocalDate.now().format(FORMATO_FECHA), fontSub);
        sub.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(sub);
 
        header.addCell(cell);
        doc.add(header);
    }
 
    private void agregarMetadatos(Document doc, String descripcion) throws Exception {
        Font font = new Font(Font.HELVETICA, 9, Font.ITALIC, COLOR_GRIS);
        Paragraph p = new Paragraph(descripcion, font);
        p.setSpacingAfter(10);
        doc.add(p);
    }
 
    private void agregarFichaCliente(Document doc, Map<String, Object> info) throws Exception {
        PdfPTable ficha = new PdfPTable(4);
        ficha.setWidthPercentage(100);
        ficha.setWidths(new int[]{20, 30, 20, 30});
        ficha.setSpacingAfter(10);
 
        Font label = new Font(Font.HELVETICA, 8, Font.BOLD, COLOR_GRIS);
        Font valor = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
 
        String[][] campos = {
            {"Tipo",      str(info, "tipo")},
            {"Teléfono",  str(info, "telefono")},
            {"Documento", str(info, "documento")},
            {"Correo",    str(info, "correo")}
        };
 
        for (String[] campo : campos) {
            PdfPCell lbl = new PdfPCell(new Phrase(campo[0], label));
            lbl.setBorder(Rectangle.BOTTOM);
            lbl.setBorderColor(COLOR_BORDE);
            lbl.setPadding(4);
            lbl.setBackgroundColor(COLOR_ROW_ALT);
 
            PdfPCell val = new PdfPCell(new Phrase(campo[1], valor));
            val.setBorder(Rectangle.BOTTOM);
            val.setBorderColor(COLOR_BORDE);
            val.setPadding(4);
 
            ficha.addCell(lbl);
            ficha.addCell(val);
        }
        doc.add(ficha);
    }
 
    private PdfPTable crearTabla(String[] cabeceras, int[] anchos, Color colorAccent) throws Exception {
        PdfPTable tabla = new PdfPTable(cabeceras.length);
        tabla.setWidthPercentage(100);
        tabla.setWidths(anchos);
        tabla.setSpacingBefore(4);
 
        Font fontHeader = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        for (String titulo : cabeceras) {
            PdfPCell cell = new PdfPCell(new Phrase(titulo, fontHeader));
            cell.setBackgroundColor(colorAccent);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPaddingTop(6);
            cell.setPaddingBottom(6);
            cell.setPaddingLeft(5);
            cell.setPaddingRight(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(cell);
        }
        tabla.setHeaderRows(1);
        return tabla;
    }
 
    private void agregarCelda(PdfPTable tabla, String texto, boolean par, int alineacion) {
        Font font = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(texto != null ? texto : "–", font));
        cell.setBackgroundColor(par ? COLOR_ROW_ALT : Color.WHITE);
        cell.setBorderColor(COLOR_BORDE);
        cell.setBorderWidth(0.5f);
        cell.setPaddingTop(5);
        cell.setPaddingBottom(5);
        cell.setPaddingLeft(5);
        cell.setPaddingRight(5);
        cell.setHorizontalAlignment(alineacion);
        tabla.addCell(cell);
    }
 
    private void agregarTotalizador(Document doc, List<Map<String, Object>> datos,
                                    String campo) throws Exception {
        double total = datos.stream().mapToDouble(fila -> {
            Object val = fila.get(campo);
            if (val == null) return 0.0;
            try { return Double.parseDouble(val.toString()); }
            catch (NumberFormatException e) { return 0.0; }
        }).sum();
 
        Font font = new Font(Font.HELVETICA, 10, Font.BOLD, COLOR_HEADER_BG);
        Paragraph p = new Paragraph(String.format("Total general: $%,.2f", total), font);
        p.setAlignment(Element.ALIGN_RIGHT);
        p.setSpacingBefore(6);
        doc.add(p);
    }
 
    private void agregarPieDePagina(PdfWriter writer) {
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter w, Document d) {
                PdfContentByte cb = w.getDirectContent();
                Font font = new Font(Font.HELVETICA, 7, Font.NORMAL, COLOR_GRIS);
 
                cb.setColorStroke(COLOR_BORDE);
                cb.setLineWidth(0.5f);
                cb.moveTo(d.left(), d.bottom() - 5);
                cb.lineTo(d.right(), d.bottom() - 5);
                cb.stroke();
 
                ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                        new Phrase("StockSólido – Reporte generado automáticamente", font),
                        d.left(), d.bottom() - 15, 0);
 
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                        new Phrase("Página " + w.getPageNumber(), font),
                        d.right(), d.bottom() - 15, 0);
            }
        });
    }
 
    // utilidades de formato
 
    private String str(Map<String, Object> fila, String clave) {
        Object val = fila.get(clave);
        return val != null ? val.toString() : "–";
    }
 
    private String fecha(Map<String, Object> fila, String clave) {
        Object val = fila.get(clave);
        if (val == null) return "–";
        if (val instanceof Date) {
            return ((Date) val).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(FORMATO_FECHA);
        }
        if (val instanceof java.sql.Date) return ((java.sql.Date) val).toLocalDate().format(FORMATO_FECHA);
        if (val instanceof LocalDate)     return ((LocalDate) val).format(FORMATO_FECHA);
        return val.toString();
    }
 
    private String moneda(Map<String, Object> fila, String clave) {
        Object val = fila.get(clave);
        if (val == null) return "$0,00";
        try { return String.format("$%,.2f", Double.parseDouble(val.toString())); }
        catch (NumberFormatException e) { return val.toString(); }
    }
 
    private String construirRango(LocalDate inicio, LocalDate fin) {
        if (inicio == null && fin == null) return "Todo el período";
        if (inicio == null) return "Hasta " + fin.format(FORMATO_FECHA);
        if (fin == null)    return "Desde " + inicio.format(FORMATO_FECHA);
        return inicio.format(FORMATO_FECHA) + " – " + fin.format(FORMATO_FECHA);
    }
}
