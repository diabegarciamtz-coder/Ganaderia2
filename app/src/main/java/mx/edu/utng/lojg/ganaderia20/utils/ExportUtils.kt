package mx.edu.utng.lojg.ganaderia20.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import mx.edu.utng.lojg.ganaderia20.data.entities.AnimalEntity
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExportUtils {

    /**
     * Genera un PDF con la lista de animales
     */
    fun generarPDFAnimales(
        context: Context,
        animales: List<AnimalEntity>,
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Crear directorio si no existe
            val directory = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "Ganaderia"
            )
            if (!directory.exists()) {
                directory.mkdirs()
            }

            // Crear archivo
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(directory, "Reporte_Animales_$timestamp.pdf")

            // Crear PDF
            val writer = PdfWriter(file)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            // Título
            document.add(
                Paragraph("REPORTE DE GANADO")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20f)
                    .setBold()
            )

            document.add(
                Paragraph("Fecha: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10f)
            )

            document.add(Paragraph("\n"))

            // Resumen
            document.add(
                Paragraph("RESUMEN")
                    .setFontSize(14f)
                    .setBold()
            )
            document.add(Paragraph("Total de animales: ${animales.size}"))
            document.add(Paragraph("Vacas: ${animales.count { it.tipo.equals("Vaca", true) }}"))
            document.add(Paragraph("Toros: ${animales.count { it.tipo.equals("Toro", true) }}"))
            document.add(Paragraph("Becerros: ${animales.count { it.tipo.equals("Becerro", true) }}"))

            document.add(Paragraph("\n"))

            // Tabla de animales
            document.add(
                Paragraph("DETALLE DE ANIMALES")
                    .setFontSize(14f)
                    .setBold()
            )

            val table = Table(UnitValue.createPercentArray(floatArrayOf(15f, 20f, 15f, 20f, 15f, 15f, 20f)))
                .useAllAvailableWidth()

            // Encabezados
            table.addHeaderCell("Arete")
            table.addHeaderCell("Nombre")
            table.addHeaderCell("Tipo")
            table.addHeaderCell("Raza")
            table.addHeaderCell("Peso (kg)")
            table.addHeaderCell("Estado")
            table.addHeaderCell("Observaciones")

            // Datos
            animales.forEach { animal ->
                table.addCell(animal.arete)
                table.addCell(animal.nombre)
                table.addCell(animal.tipo)
                table.addCell(animal.raza)
                table.addCell(animal.peso)
                table.addCell(animal.estadoSalud)
                table.addCell(animal.observaciones)
            }

            document.add(table)
            document.close()

            onSuccess(file)

        } catch (e: Exception) {
            onError("Error al generar PDF: ${e.message}")
        }
    }

    /*
    /**
     * Genera un Excel con la lista de animales
     */
    fun generarExcelAnimales(
        context: Context,
        animales: List<AnimalEntity>,
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        // Ejecutar en un hilo de fondo
        Thread {
            try {
                println("📊 Iniciando generación de Excel...")

                if (animales.isEmpty()) {
                    onError("No hay animales para exportar")
                    return@Thread
                }

                // Crear directorio
                val directory = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "Ganaderia"
                )

                if (!directory.exists()) {
                    val created = directory.mkdirs()
                    println("📁 Directorio creado: $created")
                }

                // Crear archivo
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val file = File(directory, "Reporte_Animales_$timestamp.xlsx")

                println("📄 Archivo: ${file.absolutePath}")

                // Crear workbook
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Animales")

                // Crear estilo de header
                val headerStyle = workbook.createCellStyle()
                val headerFont = workbook.createFont()
                headerFont.bold = true
                headerStyle.setFont(headerFont)

                // Headers
                val headerRow = sheet.createRow(0)
                val headers = arrayOf(
                    "Arete", "Nombre", "Tipo", "Raza", "Peso (kg)",
                    "Fecha Nac.", "Estado", "Madre", "Padre", "Observaciones"
                )

                headers.forEachIndexed { index, header ->
                    val cell = headerRow.createCell(index)
                    cell.setCellValue(header)
                    cell.cellStyle = headerStyle
                }

                // Datos
                animales.forEachIndexed { rowIndex, animal ->
                    val row = sheet.createRow(rowIndex + 1)

                    row.createCell(0).setCellValue(animal.arete)
                    row.createCell(1).setCellValue(animal.nombre)
                    row.createCell(2).setCellValue(animal.tipo)
                    row.createCell(3).setCellValue(animal.raza)
                    row.createCell(4).setCellValue(animal.peso)
                    row.createCell(5).setCellValue(animal.fechaNacimiento)
                    row.createCell(6).setCellValue(animal.estadoSalud)
                    row.createCell(7).setCellValue(animal.madre ?: "N/A")
                    row.createCell(8).setCellValue(animal.padre ?: "N/A")
                    row.createCell(9).setCellValue(animal.observaciones ?: "N/A")
                }

                // Auto-ajustar columnas
                for (i in headers.indices) {
                    sheet.autoSizeColumn(i)
                }

                // Guardar archivo
                FileOutputStream(file).use { fos ->
                    workbook.write(fos)
                }

                workbook.close()

                println("✅ Excel generado exitosamente")
                onSuccess(file)

            } catch (e: Exception) {
                println("❌ Error generando Excel: ${e.message}")
                e.printStackTrace()
                onError("Error: ${e.message}")
            }
        }.start()
    }

     */

    /**
     * Abre un archivo con la aplicación predeterminada
     */
    fun abrirArchivo(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, getMimeType(file))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Abrir con"))
        } catch (e: Exception) {
            // Si no hay app para abrir el archivo, no hacer nada
        }
    }

    /**
     * Comparte un archivo
     */
    fun compartirArchivo(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = getMimeType(file)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Compartir"))
        } catch (e: Exception) {
            // Manejar error
        }
    }

    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "pdf" -> "application/pdf"
            "xlsx", "xls" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            else -> "*/*"
        }
    }
}