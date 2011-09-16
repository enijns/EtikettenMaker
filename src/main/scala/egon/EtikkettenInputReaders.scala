package egon

import org.apache.poi.poifs.filesystem.POIFSFileSystem
import java.io.{FileInputStream, File}
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel._
import javax.swing.table.AbstractTableModel


/**
 * Created by IntelliJ IDEA.
 * User: Egon
 * Date: 9/01/11
 * Time: 8:22
 * To change this template use File | Settings | File Templates.
 */

class EtikkettenModel(val variableNames:List[String], val model:List[Map[String, String]]) extends AbstractTableModel {
  var selected:List[Boolean] = model.map(_ => true)
  override def getColumnName(col:Int) = {
    if(col==0) "include?" else variableNames(col-1)
  }
  override def getRowCount() = model.length
  override def getColumnCount() = variableNames.length + 1
  override def getValueAt(row:Int, col:Int):AnyRef = {
    if(col==0) {
      selected(row).asInstanceOf[AnyRef]
    } else {
      model(row).get(variableNames(col-1)).getOrElse(null)
    }
  }
  override def isCellEditable(row:Int, col:Int) = col == 0
  override def setValueAt(value:Any, row:Int, col:Int):Unit = {
    if(col==0) {
      selected = selected.updated(row, value.asInstanceOf[Boolean])
    }
  }
  def enkelGeselecteerde():EtikkettenModel = {
    new EtikkettenModel(
      variableNames,
      model.zipWithIndex.filter(_ match {
        case(value, index) => selected(index)
      }).map(_._1)
    )
  }
}

class EtikkettenExcelInputReader {
  def getSheets(file:File):List[(Int, String)] = {
    var res:List[(Int, String)] = Nil;
    val poiFileSystem = new POIFSFileSystem(new FileInputStream(file));
    val workbook = new HSSFWorkbook(poiFileSystem);
    for(i <- 0 to workbook.getNumberOfSheets-1) {
      res = (i, workbook.getSheetName(i)) :: res
    }
    res.reverse
  }
  def parseInputFile(file:File, sheetIndex:Int):EtikkettenModel = {
    val poiFileSystem = new POIFSFileSystem(new FileInputStream(file));
    val workbook = new HSSFWorkbook(poiFileSystem);
    val selectedSheet = workbook.getSheetAt(sheetIndex);
    val rowIterator = selectedSheet.rowIterator()
    var names:List[String] = Nil
    val rowsToSkip = getRowsWithMergedCells(selectedSheet)
    var headersFound = false
    while(rowIterator.hasNext && !headersFound) {
      val row: Row = rowIterator.next
      if(!rowsToSkip.contains(row.getRowNum)) {
        for(i <- 0 to row.getLastCellNum) {
          if(row.getCell(i) != null) {
            names = getCellValAsString(row.getCell(i)) :: names
          }
        }
        names = names.reverse
        headersFound = true
      }
    }
    var model:List[Map[String, String]] = Nil;
    while(rowIterator.hasNext) {
      val row: Row = rowIterator.next
      var map:Map[String, String] = Map();
      import scala.math._
      for(i <- 0 to min(row.getLastCellNum, (names.length-1))) {
        if(row.getCell(i) != null) {
          map += names(i) -> getCellValAsString(row.getCell(i))
        }
      }
      model = map :: model
    }
    return new EtikkettenModel(names, model)
  }

  def getRowsWithMergedCells(sheet:Sheet):Set[Int] = {
    var rowsWithMergedCells:Set[Int] = Set()
    var i = 0
    while(i < sheet.getNumMergedRegions()) {
      for(row <- sheet.getMergedRegion(i).getFirstRow to sheet.getMergedRegion(i).getLastRow) {
        rowsWithMergedCells = rowsWithMergedCells + row
      }
      i = i+1
    }
    rowsWithMergedCells
  }

  def getCellValAsString(cell:Cell):String = cell.getCellType match {
    case Cell.CELL_TYPE_NUMERIC =>
	    return (cell.getNumericCellValue).asInstanceOf[Int].toString
    case Cell.CELL_TYPE_STRING =>
      return cell.getStringCellValue
    case Cell.CELL_TYPE_BLANK =>
      return ""
    case _ =>
      return "unsupported cell type"
	}

}