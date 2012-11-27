<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="header.xsl"/>
	<xsl:include href="banner.xsl"/>
	<xsl:include href="utils.xsl"/>
	<xsl:include href="metadata.xsl"/>
	
	<xsl:param name="markupType"/>
	<xsl:param name="outputType"/>

	<xsl:template priority="100" match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
	
	<xsl:template priority="200" match="*/text()">
		<xsl:variable name="markedUp">
			<xsl:call-template name="processText">
				<xsl:with-param name="text" select="string()"/>
				<xsl:with-param name="markupType" select="$markupType"></xsl:with-param>
			</xsl:call-template>
		</xsl:variable>
		<xsl:copy>
			<xsl:value-of select="$markedUp"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>