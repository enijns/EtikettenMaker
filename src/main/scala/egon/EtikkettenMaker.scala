package egon

import swing._
import swing.event._
import java.io.{File}
import javax.swing.filechooser.{FileFilter}
import java.net.URI
import java.awt.{GridBagConstraints, Desktop}
import java.util.prefs.Preferences
import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel
import javax.swing.{ListModel, UIManager}

/**
 * Created by IntelliJ IDEA.
 * User: Egon
 * Date: 8/01/11
 * Time: 19:49
 * To change this template use File | Settings | File Templates.
 */

object EtikkettenMaker extends SimpleSwingApplication {
  try {
    UIManager.setLookAndFeel(new NimbusLookAndFeel)
  } catch {
    case _ => //whatever, default L&F should do :)
  }
	def newField = new TextField {
		text = "0"
		columns = 5
	}
  val preferences = Preferences.userNodeForPackage(getClass())
	val inputFileChooser = new FileChooser
  val inputFileText = new Label
  var inputModel = new EtikkettenModel(Nil, Nil)
  val sheetsComboBox = new ComboBox(List[(Int, String)]((1, "Geen bestand geselecteerd"))) {
    renderer = ListView.Renderer(_._2)
  }
  val resultaatLinkButton = new Button("Genereer PDF")
  val resNamenLabel = new Label("Geen bestand geladen")
  val templateTA = new TextArea
  val cellsToSkipComboBox = new ComboBox(0 to 22)
  var previewtable = new Table
  val scrollpane = new ScrollPane(previewtable) {
    minimumSize = new Dimension(260, 260)
  }
  val etikkettenInputReader = new EtikkettenExcelInputReader

	def top = new MainFrame {
		title = "Etikketten Maker"
    inputFileChooser.fileFilter = new FileFilter() {
      def accept(pathname:File) = pathname.isDirectory || pathname.getName.endsWith(".xls")
      def getDescription = "Excel bestanden (*.xls)"
    }
    inputFileChooser.fileSelectionMode = FileChooser.SelectionMode.FilesOnly
    val previousInputFile = preferences.get("xlsInputFile", "bogus")
    if(previousInputFile != "bogus") {
      inputFileChooser.selectedFile = new File(previousInputFile)
    }
    resultaatLinkButton.enabled = false
    menuBar = new MenuBar {
      contents += new Menu("File") {
        contents += new MenuItem(Action("Open excel input file") {
          chooseFile(menuBar)
        })
      }
    }
    contents = new GridBagPanel {
      import GridBagPanel._
      val c = new Constraints
      c.fill = Fill.Horizontal
      c.gridx = 0
      c.gridy = 0
      layout(sheetsComboBox) = c
      c.gridwidth = 2
      c.gridx = 0
      c.gridy = 1
      layout(resultatenPaneel) = c
      c.gridy = 2
      layout(scrollpane) = c
      c.gridy = 3
      layout(resultaatLinkButton) = c
    }
	}

  lazy val resultatenPaneel = new GridBagPanel {
    import GridBagPanel._
    val c = new Constraints
    c.fill = Fill.Horizontal
    c.gridx = 0
    c.gridy = 0
    layout(new Label("Werkblad: ")) = c
    c.gridx = 1
    c.gridy = 0
    layout(sheetsComboBox) = c
    c.gridx = 0
    c.gridy = 1
    layout(new Label("Template: ")) = c
    c.gridx = 1
    c.gridy = 1
    layout(templateTA) = c
    c.gridx = 0
    c.gridy = 2
    layout(new Label("# cellen overslaan: ")) = c
    c.gridx = 1
    c.gridy = 2
    layout(cellsToSkipComboBox) = c
  }

	listenTo(inputFileText, resultaatLinkButton, sheetsComboBox.selection)

  def chooseFile(comp:Component) = {
    if(inputFileChooser.showOpenDialog(comp) == FileChooser.Result.Approve) {
      sheetsComboBox.peer.setModel(ComboBox.newConstantModel(etikkettenInputReader.getSheets(inputFileChooser.selectedFile)))
      preferences.put("xlsInputFile", inputFileChooser.selectedFile.getPath)
    }
  }

  reactions += {
    case ButtonClicked(`resultaatLinkButton`) =>
      val model = inputModel.enkelGeselecteerde()
      val pdf = PdfGenerator.generatePdf(model, templateTA.text, cellsToSkipComboBox.selection.item)
      Desktop.getDesktop.open(pdf)
    case SelectionChanged(`sheetsComboBox`) =>
      inputModel = processInput(inputFileChooser.selectedFile, sheetsComboBox.selection.item._1)
      previewtable.peer.setModel(inputModel)
	}

  def processInput(file:File, sheetIndex:Int):EtikkettenModel = {
    inputFileText.text = file.getName
    val model = etikkettenInputReader.parseInputFile(file, sheetIndex)
    resNamenLabel.text = model.variableNames.mkString(" - ")
    templateTA.text = model.variableNames.mkString("${", "} ${", "}")
    resultaatLinkButton.enabled = true
    model
  }
}