package org.fao.geonet.services.publisher;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

public class GeopublisherConfig {
	private List<GeoserverNode> geoserverNodes;
	public void setGeoserverNodes(List<GeoserverNode> geoserverNodes) { this.geoserverNodes = geoserverNodes; }
	public List<GeoserverNode> getGeoserverNodes() { return geoserverNodes; }
	public List<Node> getNodes() { return new ArrayList<Node>(geoserverNodes); }
	public Node get(String nodeId) {
		for( Node node: getNodes()) {
			if(node.getId().equals(nodeId)) {
				return node;
			}
		}
		return null;
	}
	
	public static abstract class Node {
		private String id;
		private String name;
		private String namespacePrefix;
		private String namespaceUrl;
		private String adminUrl;
		private String wmsUrl;
		private String wfsUrl;
		private String wcsUrl;
		private String user;
		private String password;
		
		public Element toElement() {
			return new Element("node")
				.addContent(new Element("id").setText(id))
				.addContent(new Element("name").setText(name))
				.addContent(new Element("namespacePrefix").setText(namespacePrefix))
				.addContent(new Element("namespaceUrl").setText(namespaceUrl))
				.addContent(new Element("adminUrl").setText(adminUrl))
				.addContent(new Element("wmsUrl").setText(wmsUrl))
				.addContent(new Element("wfsUrl").setText(wfsUrl))
				.addContent(new Element("wcsUrl").setText(wcsUrl));
		}

		public String getId() { return id; }
		public void setId(String id) { this.id = id; }
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public String getNamespacePrefix() { return namespacePrefix; }
		public void setNamespacePrefix(String namespacePrefix) { this.namespacePrefix = namespacePrefix; }
		public String getNamespaceUrl() { return namespaceUrl; }
		public void setNamespaceUrl(String namespaceUrl) { this.namespaceUrl = namespaceUrl; }
		public String getAdminUrl() { return adminUrl; }
		public void setAdminUrl(String adminUrl) { this.adminUrl = adminUrl; }
		public String getWmsUrl() { return wmsUrl; }
		public void setWmsUrl(String wmsUrl) { this.wmsUrl = wmsUrl; }
		public String getWfsUrl() { return wfsUrl; }
		public void setWfsUrl(String wfsUrl) { this.wfsUrl = wfsUrl; }
		public String getWcsUrl() { return wcsUrl; }
		public void setWcsUrl(String wcsUrl) { this.wcsUrl = wcsUrl; }
		public String getUser() { return user; }
		public void setUser(String user) { this.user = user; }
		public String getPassword() { return password; }
		public void setPassword(String password) { this.password = password; }
	}
	public static class GeoserverNode extends Node {
		private String stylerUrl;
		public String getStylerUrl() { return stylerUrl; }
		public void setStylerUrl(String stylerUrl) { this.stylerUrl = stylerUrl; }

		@Override
		public Element toElement() {
			return super.toElement()
					.addContent(new Element("stylerUrl").setText(stylerUrl));
		}
	}
}
