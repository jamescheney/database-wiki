package org.dbwiki.data.query.visual;

public class VisualisationNode extends Node {
	public VisualisationTypeNode t;
	public ArgumentsNode args;
	public BodyNode body;
	
	public VisualisationNode(VisualisationTypeNode t, ArgumentsNode args, BodyNode body) {
		this.t = t;
		this.args = args;
		this.body = body;
	}

}
