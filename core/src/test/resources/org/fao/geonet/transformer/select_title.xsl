<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:gmd="http://www.isotc211.org/2005/gmd"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="gmd:title" priority="5">
    <xsl:copy-of select="."/>
  </xsl:template>
  <xsl:template match="node()/@*" />
</xsl:stylesheet>