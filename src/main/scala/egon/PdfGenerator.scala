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
  val myFont = new FontFactoryImp().getFont("arial", 11, new Color(0xFF00FF))
  val fontPath = getClass.getResource("/qlassik_medium_regular.ttf").getPath
  val qLassikMediumBaseFont = BaseFont.createFont(fontPath, BaseFont.WINANSI, BaseFont.EMBEDDED)
  lazy val qLassikMediumFont = {
    val font = new Font(qLassikMediumBaseFont, 12)
    font.setColor(new Color(0x40c1f3))
    font
  }
  lazy val bloemetje:Image = {
    val flower = Image.getInstance(getClass.getResource("/bloemetje_etiket.png").getPath)
    flower.scalePercent(10)
    flower
  }
  val pageMarginTop = Utilities.millimetersToPoints(4.5f)
  val pageMarginBottom = Utilities.millimetersToPoints(4.5f)
  val pageMarginLeft = Utilities.millimetersToPoints(1)
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
      doc.setMargins(pageMarginLeft, pageMarginRight, pageMarginTop, pageMarginBottom)
      doc.newPage
      createPages(model, template, cellsToSkip, doc)
    } finally {
      doc.close();
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
    var res:List[PdfPTable] = Nil
    var table = createNewTable
    for(j <- 1 to cellsToSkip) {
      table.addCell(createCell)
      i = i+1
    }
    for(map <- model.model) {
      val cell = createCell
      cell.setCellEvent(BloemetjeCellEvent)
      processTemplateAndPutInCell(cell, template, map)
      table.addCell(cell)
      i = i+1
      if(i%24==0) {
        doc.add(table)
        doc.newPage
        table = createNewTable
      }
    }
    for(j <- 0 to i%24) {
      table.addCell(createCell)
    }
    doc.add(table)
    doc.newPage
  }

  private def createCell():PdfPCell = {
    import Utilities._
    val cell = new PdfPCell
    cell.setFixedHeight(millimetersToPoints(36))
    cell.setBorder(0)
    cell.setPadding(millimetersToPoints(2))
    cell.setVerticalAlignment(Element.ALIGN_CENTER)
    cell
  }

  def processTemplateAndPutInCell(cell:PdfPCell, template:String, map:Map[String, String]):Unit = {
    for(line <- template.split("\n")) {
      var lineOutput = line
      for(variableName <- map.keys) {
        lineOutput = lineOutput.replace("${"+variableName+"}", map(variableName))
      }
      cell.addElement(new Paragraph(lineOutput, qLassikMediumFont))
    }
  }

  object BloemetjeCellEvent extends PdfPCellEvent {
    override def cellLayout (cell: PdfPCell, position: Rectangle, canvases: Array[PdfContentByte]): Unit = {
      val cb:PdfContentByte = canvases(PdfPTable.BACKGROUNDCANVAS)
      cb.addImage(bloemetje, bloemetje.getWidth/2, 0f, 0f, bloemetje.getHeight/2,
        position.getRight - bloemetje.getWidth,
        position.getBottom + 10
      );
    }
  }
}
