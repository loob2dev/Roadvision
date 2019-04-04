package au.com.CarDVR.Roadvision.FileBrowser.Model;

import org.w3c.dom.Node ;

import au.com.CarDVR.Roadvision.FileBrowser.Model.FileBrowserModel.ModelException ;

public abstract class FileBrowserNode{

	protected FileBrowserNode(Node node) throws ModelException {
		if (node != null)
			parseNode(node) ;
	}
	
	

	protected abstract void parseNode(Node node) throws ModelException ;
}