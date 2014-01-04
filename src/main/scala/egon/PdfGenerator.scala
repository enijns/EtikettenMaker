package egon

import java.io.{FileOutputStream, File}
import com.lowagie.text.{List => _, _}
import java.awt.Color
import pdf._

/**
 * Created by IntelliJ IDEA.
 * User: Egon
 * Date: 9/01/11
 * Time: 20:42
 * To change this template use File | Settings | File Templates.
 */

object PdfGenerator {
  val fontPath = getClass.getResource("/SegoePrint.ttf").getPath
  val segoePrintFont = BaseFont.createFont(fontPath, BaseFont.WINANSI, BaseFont.EMBEDDED)
  def segoePrintMediumFont = {
    val font = new Font(segoePrintFont, 9)
    font.setColor(new Color(0x00a09a))
    font
  }
  lazy val bloemetje:Image = {
    //val flower = Image.getInstance(getClass.getResource("/karroosKl.png").getPath)
    val flower = Image.getInstance(getClass.getResource("/karrooKl.png").getPath)
    //flower.scalePercent(20)
    flower
  }
  val pageMarginTop = Utilities.millimetersToPoints(4.5f)
  val pageMarginBottom = Utilities.millimetersToPoints(4.5f)
  val pageMarginLeft = Utilities.millimetersToPoints(3)
  val pageMarginRight = Utilities.millimetersToPoints(1)
  val colWidth = Utilities.millimetersToPoints(70)

  def generatePdf(model:EtikkettenModel, template:String, cellsToSkip:Int):File = {
    val doc = new Document
    val tempFile: File = File.createTempFile("etikketten", ".pdf")
    try {
      val fos = new FileOutputStream(tempFile)
      PdfWriter.getInstance(doc, fos)
      doc.open()
      doc.setPageSize(PageSize.A4)
      doc.setMargins(0, 0, 0, 0)
      doc.newPage
      createPages(model, template, cellsToSkip, doc)
    } finally {
      doc.close()
    }
    tempFile
  }

  private def createNewTable() = {
    val table:PdfPTable = new PdfPTable(3)
    table.setTotalWidth(Array(colWidth, colWidth, colWidth))
    table.setWidthPercentage(100f)
    table
  }

  private def createPages(model:EtikkettenModel, template:String, cellsToSkip:Int, doc:Document) {
    var i = 0
    var table = createNewTable()
    for(j <- 1 to cellsToSkip) {
      table.addCell(createCell())
      i = i+1
    }
    val rectanglesPerPage: Int = 21//24
    for(map <- model.model) {
      val cell = createCell()
      cell.setCellEvent(BloemetjeCellEvent)
      processTemplateAndPutInCell(cell, template, map)
      table.addCell(cell)
      i = i+1
      if(i%rectanglesPerPage==0) {
        doc.add(table)
        doc.newPage
        table = createNewTable()
      }
    }
    for(j <- 0 to i%rectanglesPerPage) {
      table.addCell(createCell())
    }
    doc.add(table)
    doc.newPage
  }

  private def createCell():PdfPCell = {
    import Utilities._
    val cell = new PdfPCell
    cell.setFixedHeight(millimetersToPoints(42.4f))
    cell.setBorder(0)
    cell.setPaddingLeft(millimetersToPoints(16))
    cell.setPaddingTop(millimetersToPoints(15))
    //cell.setPaddingLeft(millimetersToPoints(8))
    cell.setVerticalAlignment(Element.ALIGN_CENTER)
    cell
  }

  def processTemplateAndPutInCell(cell:PdfPCell, template:String, map:Map[String, String]):Unit = {
    for(line <- template.split("\n")) {
      var lineOutput = line
      for(variableName <- map.keys) {
        lineOutput = lineOutput.replace("${"+variableName+"}", map(variableName).trim)
      }
      lineOutput = lineOutput.replaceAll("\\$\\{.*?\\}", "").trim
      if(lineOutput.length > 0) {
        cell.addElement(new Paragraph(lineOutput, segoePrintMediumFont))
      }
    }
  }

  object BloemetjeCellEvent extends PdfPCellEvent {
    override def cellLayout (cell: PdfPCell, position: Rectangle, canvases: Array[PdfContentByte]): Unit = {
      val cb:PdfContentByte = canvases(PdfPTable.BACKGROUNDCANVAS)
      cb.addImage(bloemetje, bloemetje.getWidth, 0f, 0f, bloemetje.getHeight,
        position.getLeft + 18,
        position.getTop - 63
      )
    }
  }
}
