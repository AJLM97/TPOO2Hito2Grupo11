package edu.unla.epicentro_gourmet.services.implementation;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import edu.unla.epicentro_gourmet.dtos.LiquidacionHaberesDTO;
import edu.unla.epicentro_gourmet.entities.Empleado;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.repositories.IEmpleadoRepository;
import edu.unla.epicentro_gourmet.services.ILiquidacionService;
import edu.unla.epicentro_gourmet.services.IReciboSueldoService;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Service
public class ReciboSueldoService implements IReciboSueldoService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final IEmpleadoRepository empleadoRepository;
    private final ILiquidacionService liquidacionService;

    public ReciboSueldoService(IEmpleadoRepository empleadoRepository, ILiquidacionService liquidacionService) {
        this.empleadoRepository = empleadoRepository;
        this.liquidacionService = liquidacionService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<byte[]> generarRecibo(String emailEmpleado) {
        Optional<Empleado> empleado = empleadoRepository.findByUsuario_EmailAndActivoTrue(emailEmpleado);
        if (empleado.isEmpty()) {
            return Optional.empty();
        }
        Optional<LiquidacionHaberesDTO> liquidacion = liquidacionService.liquidar(empleado.get());
        if (liquidacion.isEmpty()) {
            return Optional.empty();
        }
        UnidadVenta unidad = (UnidadVenta) Hibernate.unproxy(empleado.get().getUnidad());
        Festival festival = (Festival) Hibernate.unproxy(unidad.getFestival());
        return Optional.of(armarPdf(empleado.get(), unidad, festival, liquidacion.get()));
    }

    private byte[] armarPdf(Empleado empleado, UnidadVenta unidad, Festival festival, LiquidacionHaberesDTO liq) {
        NumberFormat pesos = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));
        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
        Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            Paragraph encabezado = new Paragraph("Epicentro Gourmet - Recibo de sueldo", titulo);
            encabezado.setSpacingAfter(5);
            documento.add(encabezado);
            Paragraph emision = new Paragraph("Emitido el " + LocalDate.now().format(FORMATO_FECHA), normal);
            emision.setSpacingAfter(15);
            documento.add(emision);

            documento.add(new Paragraph("Datos del empleado", subtitulo));
            PdfPTable datos = nuevaTabla();
            fila(datos, "Nombre", liq.getNombreCompleto(), negrita, normal);
            fila(datos, "DNI", String.valueOf(empleado.getDni()), negrita, normal);
            fila(datos, "Rol", liq.getRol(), negrita, normal);
            fila(datos, "Fecha de ingreso", empleado.getFechaIngreso().format(FORMATO_FECHA), negrita, normal);
            documento.add(datos);

            documento.add(new Paragraph("Festival y unidad de venta", subtitulo));
            PdfPTable lugar = nuevaTabla();
            fila(lugar, "Festival", festival.getNombre(), negrita, normal);
            fila(lugar, "Período", festival.getFechaInicio().format(FORMATO_FECHA) + " al "
                    + festival.getFechaFin().format(FORMATO_FECHA) + " (" + liq.getDiasFestival() + " días)", negrita, normal);
            fila(lugar, "Unidad de venta", unidad.getNombreComercial() + " (" + unidad.getCodigo() + ")", negrita, normal);
            documento.add(lugar);

            documento.add(new Paragraph("Liquidación", subtitulo));
            PdfPTable detalle = nuevaTabla();
            fila(detalle, "Sueldo base", pesos.format(liq.getSueldoBase()), negrita, normal);
            fila(detalle, liq.getConceptoAdicional(), pesos.format(liq.getAdicional()), negrita, normal);
            fila(detalle, "Porcentaje según duración", Math.round(liq.getPorcentaje() * 100) + "%", negrita, normal);
            fila(detalle, "TOTAL A COBRAR", pesos.format(liq.getTotal()), negrita, negrita);
            documento.add(detalle);
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el recibo de sueldo", e);
        } finally {
            documento.close();
        }
        return salida.toByteArray();
    }

    private PdfPTable nuevaTabla() {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(6);
        tabla.setSpacingAfter(15);
        return tabla;
    }

    private void fila(PdfPTable tabla, String etiqueta, String valor, Font fuenteEtiqueta, Font fuenteValor) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, fuenteEtiqueta));
        celdaEtiqueta.setPadding(6);
        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fuenteValor));
        celdaValor.setPadding(6);
        tabla.addCell(celdaEtiqueta);
        tabla.addCell(celdaValor);
    }
}