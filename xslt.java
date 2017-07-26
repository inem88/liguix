import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class xslt extends JFrame 
                  implements WindowListener, KeyListener,      DropTargetListener, 
                             ActionListener, DocumentListener, UndoableEditListener,
                             MouseListener{

	private static final int version = 1;
	private static final long serialVersionUID = 20120629L;
	public static final Font  textFont       = new Font("Courier New", Font.PLAIN | Font.TRUETYPE_FONT, 12);
	public static final Font  labelFont      = new Font("Courier New", Font.BOLD  | Font.TRUETYPE_FONT, 20);
	public static final Font  fileLabelFont  = new Font("Courier New", Font.PLAIN | Font.TRUETYPE_FONT, 12);
	public static final Color labelBGColor   = new Color(200, 200, 255);
	public static final int   INITIAL_WIDTH  = 800;
	public static final int   INITIAL_HEIGTH = 600;

	public static final String defaultInputFileName = "data.xml";
	public static final String defaultXslFileName   = "process.xsl";
	
	private static final DefaultHighlighter.DefaultHighlightPainter searchHighlightPainter =
			new DefaultHighlighter.DefaultHighlightPainter(Color.red);
	
	private boolean actualViewIsVertical;
	
	private Dialog  alertModal = new Dialog(this, "ERROR", true);
	private JLabel  alertLabel = new JLabel("", JLabel.CENTER);
	private JButton alertOK    = new JButton ("OK");
	
	private Dialog     getInfoModal    = new Dialog(this, "Tilte", true);
	private JLabel     getInfoTitle    = new JLabel("", JLabel.CENTER);
	private JLabel     getInfoSubtitle = new JLabel("", JLabel.CENTER);
	private JTextField getInfoText     = new JTextField("");  
	private JButton    getInfoOK       = new JButton ("OK");
	private JButton    getInfoCANCEL   = new JButton ("Cancel");
		
	private JPanel xmlInPanel = new JPanel(new BorderLayout());
	private JPanel xsltPanel  = new JPanel(new BorderLayout());
	private JPanel outPanel   = new JPanel(new BorderLayout());
	
	private JTextArea xmlInArea = new JTextArea();
	private JTextArea xsltArea  = new JTextArea();
	private JTextArea outArea   = new JTextArea();
	
	private JTextArea xmlInLines = new JTextArea("1");
	private JTextArea xsltLines  = new JTextArea("1");	
	
	private JLabel  xmlFileLabel  = new JLabel();
	private JLabel  xsltFileLabel = new JLabel();
	private JLabel  outFileLabel  = new JLabel();
	
	private JButton xmlFileButton  = new JButton("Save");
	private JButton xsltFileButton = new JButton("Save");
	
	private UndoManager xmlUndoManager  = new UndoManager();
	private UndoManager xsltUndoManager = new UndoManager();
		
	private int secondsLeftToProcess;
	
	private String xmlLastTextToSearch=null;
	private String xsltLastTextToSearch=null;	
	
	TransformerFactory transfomerFactory = TransformerFactory.newInstance();
	
	private xslt(){super("LIGUIX V."+version);}
	
	private void startGUI()throws Exception{
		File file = new File("");
		
		// XML IN
		JLabel    xmlInLabel = new JLabel("XML IN", JLabel.CENTER);
		xmlInLines.setBackground( Color.white );
		xmlInLines.setForeground( Color.LIGHT_GRAY );
		xmlInLines.setEditable(false);
		xmlInLines.setFont(textFont);
		xmlInLines.setBorder(  BorderFactory.createLineBorder( Color.LIGHT_GRAY ) );
		JScrollPane xmlInJsrcoll = new JScrollPane( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		xmlInArea.setName("XML IN");
		xmlInArea.setFont( textFont );
		xmlInArea.addKeyListener(this);
		xmlInArea.getDocument().addDocumentListener(this);
		new DropTarget(xmlInArea, this);
		xmlInArea.getDocument().addUndoableEditListener(this);
		xmlInLabel.setFont( labelFont );
		xmlInJsrcoll.getViewport().add( xmlInArea );
		xmlInJsrcoll.setRowHeaderView( xmlInLines );
		xmlFileLabel.setText(file.getAbsolutePath()+File.separatorChar+defaultInputFileName);
		xmlFileLabel.setFont(fileLabelFont);
		xmlFileLabel.addMouseListener(this);
		xmlFileButton.setFont(fileLabelFont);
		JPanel xmlInSouth = new JPanel(new BorderLayout());
		xmlInSouth.add(BorderLayout.WEST, xmlFileButton);
		xmlInSouth.add(BorderLayout.CENTER, xmlFileLabel);
		xmlInPanel.add( BorderLayout.NORTH, xmlInLabel );
		xmlInPanel.add( BorderLayout.CENTER, xmlInJsrcoll );
		xmlInPanel.add( BorderLayout.SOUTH, xmlInSouth );
		xmlInPanel.setBorder(  BorderFactory.createRaisedBevelBorder() );
		xmlFileButton.addActionListener( this );
		
		// XSLT
		JLabel    xsltLabel = new JLabel("XSLT", JLabel.CENTER);
		xsltLines.setBackground( Color.white );
		xsltLines.setForeground( Color.LIGHT_GRAY );
		xsltLines.setEditable(false);
		xsltLines.setFont( textFont );
		xsltLines.setBorder(  BorderFactory.createLineBorder( Color.LIGHT_GRAY ) );
		JScrollPane xsltJsrcoll = new JScrollPane( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		xsltArea.setName("XSLT");
		xsltArea.setFont( textFont );
		xsltArea.addKeyListener(this);
		xsltArea.getDocument().addDocumentListener(this);
		new DropTarget(xsltArea, this);
		xsltArea.getDocument().addUndoableEditListener(this);
		xsltLabel.setFont( labelFont );
		xsltJsrcoll.getViewport().add( xsltArea );
		xsltJsrcoll.setRowHeaderView( xsltLines );
		xsltFileLabel.setText(file.getAbsolutePath()+File.separatorChar+defaultXslFileName);
		xsltFileLabel.setFont(fileLabelFont);
		xsltFileLabel.addMouseListener(this);
		xsltFileButton.setFont(fileLabelFont);
		JPanel xsltSouth = new JPanel(new BorderLayout());
		xsltSouth.add(BorderLayout.WEST, xsltFileButton);
		xsltSouth.add(BorderLayout.CENTER, xsltFileLabel);
		xsltPanel.add( BorderLayout.NORTH, xsltLabel );
		xsltPanel.add( BorderLayout.CENTER, xsltJsrcoll );
		xsltPanel.add( BorderLayout.SOUTH, xsltSouth );
		xsltFileButton.addActionListener( this );
		
		// OUT
		JLabel    outLabel = new JLabel("OUT", JLabel.CENTER);
		JScrollPane outJsrcoll = new JScrollPane( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		outArea.setName("OUT");
		outArea.setFont( textFont );
		outArea.setEditable(false);
		outFileLabel.setText( getOutAbsoluteFileName( xmlFileLabel.getText(), xsltFileLabel.getText() ));
		outFileLabel.setFont(fileLabelFont);
		outLabel.setFont( labelFont );
		outJsrcoll.getViewport().add( outArea );
		outPanel.add( BorderLayout.NORTH, outLabel );
		outPanel.add( BorderLayout.CENTER, outJsrcoll );
		outPanel.add( BorderLayout.SOUTH, outFileLabel );
		outPanel.setBorder( BorderFactory.createLoweredBevelBorder() );
		
		// Alert
		ImageIcon iconErr = new ImageIcon(Base64.decode (iconErrorStr));
		alertLabel.setBackground( Color.white );
		alertOK.addActionListener(this);
		FlowLayout okLayout = new FlowLayout();
		okLayout.setVgap(5);
		JPanel okPanel = new JPanel(okLayout);
		okPanel.setBackground( Color.white );
		okPanel.add(alertOK);
		alertModal.setLayout(new BorderLayout());
		alertModal.setBackground(Color.white);
		JPanel alertCenter = new JPanel(new FlowLayout());
		JLabel alertIcon = new JLabel(iconErr);
		alertCenter.setBackground(Color.white);
		alertCenter.add(alertIcon);
		alertCenter.add(alertLabel);
		alertModal.add(BorderLayout.CENTER, alertCenter);		
		alertModal.add(BorderLayout.SOUTH, okPanel);
		alertModal.setAlwaysOnTop(true);
		
		// Get Information
		ImageIcon iconGetInfo = new ImageIcon(Base64.decode (iconGetInfoStr));
		getInfoOK.addActionListener(this);
		getInfoCANCEL.addActionListener(this);
		getInfoModal.setLayout(new BorderLayout());
		JPanel getInfoSouth = new JPanel(new FlowLayout(FlowLayout.CENTER));
		getInfoSouth.setBackground(Color.white);
		getInfoSouth.add(getInfoOK);
		getInfoSouth.add(new JLabel("     "));
		getInfoSouth.add(getInfoCANCEL);
		getInfoTitle.setFont(new Font(getInfoTitle.getFont().getFontName(), Font.BOLD | Font.TRUETYPE_FONT, 18 ));
		JPanel getInfoCenter = new JPanel(new GridLayout(4, 1));
		getInfoCenter.setBackground( Color.white );
		getInfoCenter.add(getInfoTitle);
		getInfoCenter.add(getInfoSubtitle);
		getInfoCenter.add( new JPanel(new FlowLayout(FlowLayout.CENTER)).add( getInfoText ) );
		getInfoCenter.add(getInfoSouth);
		getInfoText.addKeyListener(this);
		getInfoModal.add(BorderLayout.WEST, new JLabel(iconGetInfo));
		getInfoModal.add(BorderLayout.CENTER, getInfoCenter);
		getInfoModal.add(BorderLayout.EAST, new JLabel("         "));
		getInfoModal.setAlwaysOnTop(true);
		getInfoModal.setBackground(Color.white);
		
		// Icon 		
		Image icon = ImageIO.read( new ByteArrayInputStream( Base64.decode (iconStr) )  );
		setIconImage(icon);

		// Final
		prechargeFiles();
		actualViewIsVertical=true;
		setHorizontalView();
		xsltFileButton.setEnabled(false);
		xmlFileButton.setEnabled(false);

		this.setBackground( labelBGColor );
		this.addWindowListener(this);
		this.getContentPane().addKeyListener(this);
	}
	
	private void setHorizontalView(){
		if(!actualViewIsVertical) return;
		
		setVisible(false);
		JPanel  mainPanelHor = new JPanel(new GridLayout(1, 3));
		this.getContentPane().removeAll();
		mainPanelHor.add(xmlInPanel);
		mainPanelHor.add(xsltPanel);
		mainPanelHor.add(outPanel);
		this.getContentPane().add( BorderLayout.CENTER, mainPanelHor );
		this.setSize(INITIAL_WIDTH, INITIAL_HEIGTH);	
		setVisible(true);
		actualViewIsVertical=false;
	}

	private void setVerticalView(){
		if(actualViewIsVertical) return;
		
		setVisible(false);		
		JPanel  mainPanelVer = new JPanel(new GridLayout(3, 1));
		this.getContentPane().removeAll();
		mainPanelVer.add(xmlInPanel);
		mainPanelVer.add(xsltPanel);
		mainPanelVer.add(outPanel);
		this.getContentPane().add( BorderLayout.CENTER, mainPanelVer );
		this.setSize(INITIAL_WIDTH, INITIAL_HEIGTH);
		setVisible(true);
		actualViewIsVertical=true;
	}

	private void goProcess(){
		String xmlInStr = xmlInArea.getText();
		String xsltStr  = xsltArea.getText();

		xmlInStr = xmlInStr.trim();
		xsltStr  = xsltStr.trim();
		outArea.setText("");
		
		if( xmlInStr.length()==0 || xsltStr.length()==0 ){
			return;
		}

		CharArrayReader xmlReader = new CharArrayReader(xmlInStr.toCharArray()); 
		CharArrayReader xsltReader = new CharArrayReader(xsltStr.toCharArray());
		CharArrayWriter outWriter = new CharArrayWriter();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		PrintStream ps = new PrintStream( baos );
		PrintStream err = System.err;
		System.setErr(ps);
		
		try{			
			Source xslt = new StreamSource(xsltReader);
			Source xml = new StreamSource(xmlReader);
			StreamResult out = new StreamResult(outWriter);
			
			Transformer transformer = transfomerFactory.newTransformer(xslt);
			transformer.transform(xml, out);

			outArea.setForeground( Color.black );
			String result = outWriter.toString();
			outArea.setText(result);
			
			writeCompleteFile(new File(outFileLabel.getText()), result);
		}catch(IOException ioe){
			outArea.setForeground( Color.red );
			outArea.setText(ioe.getClass().getName()+"\n"+ioe.getMessage());
		}catch(Exception e){
			outArea.setForeground( Color.red );
			outArea.setText(baos.toString());
		}finally{
			try{xmlReader.close();}catch (Exception e) {}
			try{xsltReader.close();}catch (Exception e) {}
			try{outWriter.close();}catch (Exception e) {}
			try{baos.close();}catch (Exception e) {}
			try{ps.close();}catch (Exception e) {}
			System.setErr(err);
		}
	}
	
	public void run(){
		for(;;){
			try{Thread.sleep(1000);}catch (Exception e) {}
			if(mustProcess()){
				goProcess();
			}
		}
	}
	
	private synchronized boolean mustProcess(){
		if(secondsLeftToProcess < 0) return false;
		secondsLeftToProcess--;
		if(secondsLeftToProcess == 0) return true;
		return false;
	}
	
	public static void main(String arg[])throws Exception{
		xslt XSLT = new xslt();
		XSLT.startGUI();		
		XSLT.run();
	}
	
	private void prechargeFiles(){
		File file;
		String content;
		
		try{
			file = new File(xmlFileLabel.getText());
			content = readCompleteFile(file);
			xmlInArea.setText(content);
			file = new File(xsltFileLabel.getText());
			content = readCompleteFile(file);
			xsltArea.setText(content);			
		}catch (Exception e) {
			System.out.println(e.getClass().getName());
			System.out.println(e.getMessage());
		}
	}
	
	public String readCompleteFile(File file)throws IOException{
		StringBuffer res = new StringBuffer();
		
		// Leo todo el archivo
		char[] buff = new char[1024];
		BufferedReader in = new BufferedReader(new FileReader(file));
		for(;;){
			int r = in.read(buff);
			if (r <=0 )break;
			res.append(buff, 0, r);
		}
		in.close();
		return res.toString();
	}
	
	public void writeCompleteFile(File file, String content)throws IOException{
		OutputStream out = new FileOutputStream(file);
		out.write(content.getBytes());
		try{out.close();}catch (Exception e) {}
	}
	
	public String getOutAbsoluteFileName(String xmlStr, String xslStr){
		File xml = new File(xmlStr);
		File xsl = new File(xslStr);
		
		String xmlName = xml.getName();
		String xslName = xsl.getName();
		
		int xmlInd = xmlName.lastIndexOf('.');
		int xslInd = xslName.lastIndexOf('.');
		xmlInd =  xmlInd<0?xmlName.length():xmlInd;
		xslInd =  xslInd<0?xslName.length():xslInd;
		
		return xml.getParentFile().getPath()+File.separatorChar+xmlName.substring(0, xmlInd)+"_"+xslName.substring(0, xslInd)+".txt" ;
	}
	
	private void alert(String title, String text){
		alertModal.setTitle(title);
		alertLabel.setText(text);
		alertModal.setLocation( this.getX()+100, this.getY()+100);
		alertModal.pack();
		alertModal.setVisible(true);
	}

	private String getInfoFromUser(String title, String subtitle, String defaultText){
		getInfoModal.setTitle(title);
		getInfoTitle.setText(title);
		getInfoSubtitle.setText(subtitle);
		getInfoText.setText(defaultText);
		getInfoModal.setLocation( this.getX()+100, this.getY()+100);
		getInfoModal.pack();
		getInfoText.requestFocusInWindow();
		getInfoModal.setVisible(true);
		getInfoText.requestFocusInWindow();
		
		return getInfoText.getText();
	}
	
	private int searchTextAllOccurrences(JTextArea textArea, String textToSearch){
		Highlighter higlighter = textArea.getHighlighter();
		higlighter.removeAllHighlights();
 
 	    Pattern pattern = Pattern.compile(textToSearch);
 	    Matcher matcher = pattern.matcher(textArea.getText()); 

 	    int count=0;
		int indInit=-1;
 	    while(matcher.find()){
 	    	count++;
			int ind1 = matcher.start();
			int ind2 = matcher.end();
			try{
				higlighter.addHighlight(ind1, ind2, searchHighlightPainter);
			}catch (Exception e){
				e.printStackTrace();
			}
			if(indInit < 0){
				indInit = ind1;
			}
 	    }
		if(indInit>0){
			textArea.setCaretPosition(indInit + textToSearch.length());
		} 	    
		if(count==0){
			alert("Not Found", "The text '"+textToSearch+"' was not found into: "+textArea.getName());
		}
		return indInit;
	}
	
	private void saveTextAreaToFile(JLabel jlabelname, JTextArea textArea){
		try{
			File file = new File (jlabelname.getText());
			writeCompleteFile(file, textArea.getText());
			
			if(textArea==xmlInArea){
				xmlFileButton.setEnabled(false);
			}
			if(textArea==xsltArea){
				xsltFileButton.setEnabled(false);
			}
		}catch (Exception e) {
			alert("IO ERROR", "<html>IO ERROR:<br>"+e.getClass().getName()+"<br>"+e.getMessage()+"</html>");
		}
	}
	
	private synchronized void triggerProcessJob(){
		if( xmlInArea.getText().length() > 50000 || 
			xsltArea.getText().length() > 50000 ){
			outArea.setText("Processing...");
			secondsLeftToProcess=3;			
		}else{
			goProcess();
		}
	}
	
	private String getTextToSearch(String name, String lastText){
		String strToSearch = getInfoFromUser("Search", "Enter the text to search into: "+name, lastText);
		strToSearch = strToSearch.trim();
		if(strToSearch.length() == 0){
			return null;
		}
		return strToSearch;
	}
	
	// Eventos de ventana
	public void windowClosed(WindowEvent e) {	}	
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {	}
	public void windowActivated(WindowEvent e) {	}
	public void windowDeactivated(WindowEvent e) {	}
	public void windowOpened(WindowEvent e) {	}	
	public void windowClosing(WindowEvent e) {
		System.exit(0);
	}
	
	// Eventos de teclado
	public void keyTyped(KeyEvent e) {	}
	public void keyReleased(KeyEvent e) {	}
	public void keyPressed(KeyEvent e) {
		/*		
		if(e.isAltDown()){
			System.out.println("Alt+Key: char="+(int)e.getKeyChar()+" code="+e.getKeyCode()+" mod="+e.getModifiers());
		}

		if(e.isControlDown()){
        	System.out.println("Ctrl+Key: char="+(int)e.getKeyChar()+" code="+e.getKeyCode()+" mod="+e.getModifiers() +"  Src="+e.getSource());
		}
		*/
		
		// Enter en get info user
		if( e.getSource()==getInfoText && (e.getKeyCode()==10 || e.getKeyCode()==13) ){
			getInfoModal.setVisible(false);	
		}
		
		// F3
		if( e.getKeyChar()==65535 && e.getKeyCode()==114 ){
			if( e.getSource()==xmlInArea && xmlLastTextToSearch != null ){
				int ind = xmlInArea.getText().indexOf(xmlLastTextToSearch, xmlInArea.getCaretPosition());
				if(ind>=0){
					xmlInArea.setCaretPosition(ind+xmlLastTextToSearch.length());
				}else{
					Toolkit.getDefaultToolkit().beep();
				}
			}
			if( e.getSource()==xsltArea && xsltLastTextToSearch != null ){
				int ind =  xsltArea.getText().indexOf(xsltLastTextToSearch, xsltArea.getCaretPosition());
				if(ind>=0){
					xsltArea.setCaretPosition(ind+xsltLastTextToSearch.length());
				}else{
					Toolkit.getDefaultToolkit().beep();
				}
			}			
		}
		
		// Ctrl-f
		if( e.getKeyChar()==6 && e.getKeyCode()==70 &&  e.isControlDown() ){
			if(e.getSource() == xmlInArea){
				String textToSearch = getTextToSearch(xmlInArea.getName(), xmlLastTextToSearch);				
				if(textToSearch != null){
					int ind = searchTextAllOccurrences(xmlInArea, textToSearch);
					if(ind >= 0){
						xmlLastTextToSearch = textToSearch;
					}
				}
			}
			if(e.getSource() == xsltArea){
				String textToSearch = getTextToSearch(xsltArea.getName(), xsltLastTextToSearch);				
				if(textToSearch != null){
					int ind = searchTextAllOccurrences(xsltArea, textToSearch);
					if(ind >= 0){
						xsltLastTextToSearch = textToSearch;
					}
				}
			}
		}				
		
		// Ctrl-s
		if( e.getKeyChar()==19 && e.getKeyCode()==83 && e.isControlDown() ){
			if(e.getSource() == xmlInArea){
				saveTextAreaToFile(xmlFileLabel, xmlInArea);
			}
			if(e.getSource() == xsltArea){
				saveTextAreaToFile(xsltFileLabel, xsltArea);
			}
		}		
		
		// Ctrl-z
		if( e.getKeyChar()==26 &&  e.isControlDown() ){
			try{
				if(e.getSource() == xmlInArea){
					xmlUndoManager.undo();
				}
				if(e.getSource() == xsltArea){
					xsltUndoManager.undo();
				}
			}catch (CannotUndoException cre) {
				Toolkit.getDefaultToolkit().beep();
			}				
		}
		// Ctrl-y
		if( e.getKeyChar()==25 &&  e.isControlDown() ){
			try{
				if(e.getSource() == xmlInArea){
					xmlUndoManager.redo();
				}
				if(e.getSource() == xsltArea){
					xsltUndoManager.redo();
				}
			}catch (CannotRedoException cre) {
				Toolkit.getDefaultToolkit().beep();
			}
		}
		
		if(e.isAltDown() &&  (int)e.getKeyChar()==65535 && e.getKeyCode()==38){
			setVerticalView();
		}
		if(e.isAltDown() &&  (int)e.getKeyChar()==65535 && e.getKeyCode()==39){
			setHorizontalView();
		}
	}
	
	// Eventos de Drag & Drop
	public void dragEnter(DropTargetDragEvent dtde){	}
	public void dragOver(DropTargetDragEvent dtde){	}
	public void dropActionChanged(DropTargetDragEvent dtde){}
	public void dragExit(DropTargetEvent dte){	}
	public void drop(DropTargetDropEvent dtde){
		try{ 			
			dtde.acceptDrop(DnDConstants.ACTION_COPY);
			@SuppressWarnings("unchecked")
			List<File> files = (List<File>)dtde.getTransferable().getTransferData( DataFlavor.javaFileListFlavor );
			if( files.size() == 1 ){
				File file = files.get(0);
				if (file.exists() && file.canWrite() && file.isFile()) try{
					if( dtde.getDropTargetContext().getComponent() == xmlInArea ){ 
						xmlInArea.setText( readCompleteFile( file ) );
						xmlFileLabel.setText( file.getAbsolutePath() );
						outFileLabel.setText( getOutAbsoluteFileName( xmlFileLabel.getText(), xsltFileLabel.getText() ));
						xmlFileButton.setEnabled(false);
					}
					if( dtde.getDropTargetContext().getComponent() == xsltArea ){
						xsltArea.setText( readCompleteFile( file ) );
						xsltFileLabel.setText( file.getAbsolutePath() );
						outFileLabel.setText( getOutAbsoluteFileName( xmlFileLabel.getText(), xsltFileLabel.getText() ));
						xsltFileButton.setEnabled(false);
					}
				}catch (IOException e) {
					alert("IO ERROR", "<html>IO ERROR:<br>"+e.getClass().getName()+"<br>"+e.getMessage()+"</html>");
				}
			}
		}catch (Exception e) {	}
	}
	
	// Eventos de los botones
	public void actionPerformed(ActionEvent act){
		if(act.getSource() == xmlFileButton ){
			saveTextAreaToFile(xmlFileLabel, xmlInArea);
		}
		if(act.getSource() == xsltFileButton ){
			saveTextAreaToFile(xsltFileLabel, xsltArea);
		}
		if( act.getSource()==alertOK){
			alertModal.setVisible(false);
		}
		if( act.getSource()==getInfoOK){
			getInfoModal.setVisible(false);
		}
		if( act.getSource()==getInfoCANCEL){
			getInfoText.setText("");
			getInfoModal.setVisible(false);
		}		
	}

	// Eventos de documento
	public  void insertUpdate(DocumentEvent e) {documentEventProcess(e);}
	public  void removeUpdate(DocumentEvent e) {documentEventProcess(e);}
	public  void changedUpdate(DocumentEvent e){documentEventProcess(e);}
	private void documentEventProcess(DocumentEvent e) {
		JTextArea textArea;
		JTextArea linesTextArea;
		if(e.getDocument()==xmlInArea.getDocument()){
			textArea = xmlInArea;
			linesTextArea = xmlInLines;
			xmlFileButton.setEnabled(true);
		}else{
			textArea = xsltArea;
			linesTextArea = xsltLines;
			xsltFileButton.setEnabled(true);
		}
		int caretPosition = textArea.getDocument().getLength();
		Element root = textArea.getDocument().getDefaultRootElement();
		String text = "1" + System.getProperty("line.separator");
		for(int i = 2; i < root.getElementIndex( caretPosition ) + 2; i++){
			text += i + System.getProperty("line.separator");
		}
		linesTextArea.setText(text);
		triggerProcessJob();
	}
	
	// Evento de UNDO & REDO
	public void undoableEditHappened(UndoableEditEvent e){
		if( e.getSource()==xmlInArea.getDocument() ) xmlUndoManager.addEdit(e.getEdit());
		if( e.getSource()==xsltArea.getDocument()  ) xsltUndoManager.addEdit(e.getEdit());
	}

	// Eventos de mouse
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseClicked(MouseEvent e){
		if(e.getSource()==xmlFileLabel && e.getClickCount()==2){
			changeFileName(xmlFileLabel);
		}
		if(e.getSource()==xsltFileLabel && e.getClickCount()==2){
			changeFileName(xsltFileLabel);
		}
	}
	private void changeFileName(JLabel label){
		String newFileName = getInfoFromUser("File Name", "Please enter the name of the file name:", label.getText());
		newFileName = newFileName.trim();
		if(newFileName.length()==0) return;
		if(newFileName.equals(label.getText())) return;
		// Miro si el nombre dado es v\E1lido
		File file = new File(newFileName);
		if(file.exists() && file.isDirectory()){
			alert("Error", "The name provided is a folder.");
			return;
		}
		label.setText(newFileName);
	}
	
	private static final String iconGetInfoStr=
		"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"+
		"M1f/D+2A9cm9dzuNAAAAAElFTkSuQmCC";
	private static final String iconErrorStr=
		"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"+
		"AAAASUVORK5CYII=";
	private static final String iconStr=
		"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"+
		"SUVORK5CYII=";
}
