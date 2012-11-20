<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gts="http://www.isotc211.org/2005/gts" 
	xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gmx="http://www.isotc211.org/2005/gmx" 
	xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gml="http://www.opengis.net/gml">
	<xsl:template mode="permitMarkup" priority="10" match="gmd:abstract | gmd:description | gmd:statement | gmd:purpose | gmd:supplementalInformation">
		<xsl:value-of select="true()" />
	</xsl:template>
	<xsl:template mode="permitMarkup" match="*">
		<xsl:value-of select="name(.)" />
	</xsl:template>
</xsl:stylesheet>