package au.com.CarDVR.Roadvision.FileBrowser.Model;

import org.w3c.dom.Node ;
import org.w3c.dom.NodeList ;

import au.com.CarDVR.Roadvision.FileBrowser.Model.FileBrowserModel.ModelException ;
import au.com.CarDVR.Roadvision.FileBrowser.Model.FileViewerElement.FileElement ;

import android.util.Log ;

public class FileNode extends FileBrowserNode  {


	public enum Format {
		
		mov, avi, mp4, jpeg, all,reardir,Rnormal,Fsos,Rsos;
	}
	public enum Action {

		dir,reardir
	}
	public enum Property {
		DCIM, Event, Photo;
	}

	public String mName ;
	public Format mFormat ;
	public int mSize ;
	public String mAttr ;
	public String mTime ;
	public long mModifiedTime;
	public boolean mSelected ;
	
	FileNode(Node fileNode) throws ModelException {
		
		super(fileNode) ;
		
		mSelected = false ;
	}
	
	public FileNode(String name, Format format, int size, String attr, String time) throws ModelException {
		
		super(null) ;
		
		mName = name ;
		mFormat = format ;
		mSize = size ;
		mAttr = attr ;
		mTime = time ;
		mSelected = false ;
	}


	@Override
	protected void parseNode(Node node) throws ModelException {

		NodeList children = node.getChildNodes() ;
		
		mName = null ;
		mFormat = null ;
		mSize = 0 ;
		mAttr = null ;
		mTime = null ;
		
		String name = null ;
		String format = null ;
		String size = null ;
		String attr = null ;
		String time = null ;
		
		for (int i = 0 ; i < children.getLength() ; i++) {
			Node child = children.item(i) ;

			if (child.getNodeType() != Node.ELEMENT_NODE)
				continue ;

			if (FileElement.name.matchElement(child)) {
				name = child.getTextContent() ;
			} else if (FileElement.format.matchElement(child)) {
				format = child.getTextContent() ;
			} else if (FileElement.size.matchElement(child)) {
				size = child.getTextContent() ;
			} else if (FileElement.attr.matchElement(child)) {
				attr = child.getTextContent() ;
			} else if (FileElement.time.matchElement(child)) {
				time = child.getTextContent() ;
			} else {
				Log.i("FileNode", "Ignoring unknown element: " + node.getNodeName() 
						+ "/"+ child.getNodeName()) ;
			}
		}
		
		if (name == null || format == null || size == null || attr == null || time == null) {
			throw new ModelException() ;
		}
		
		mName = name ;
		mFormat = FileBrowserModel.strToEnum(Format.class, format) ;
		mSize = Integer.valueOf(size) ;
		mAttr = attr ;
		mTime = time ;
		
	}
}
