CREATE TABLE MaintenanceReport
  (
    id         varchar2(256) NOT NULL,
	taskClass  varchar2(64),
    category   NUMBER(10) NOT NULL,
	severity   NUMBER(10) NOT NULL,
	ignored    CHAR(1) NOT NULL,
	CONSTRAINT MaintenanceReport_pk PRIMARY KEY(id)
  );
  
CREATE TABLE MaintenanceReportLocalization 
  (
    reportid        NUMBER(10),
    langid			varchar2(3) NOT NULL,
	name            varchar2(32) NOT NULL,
    description     varchar2(1048),

	CONSTRAINT MaintenanceReportLocal_pk PRIMARY KEY(reportId, langid),
    CONSTRAINT MaintenanceReportLocal_fk FOREIGN KEY(reportid) references MaintenanceReport(id)
  );
 
CREATE TABLE MaintenanceParams
  (
    reportid        NUMBER(10),
	id				NUMBER(10),
	name			varchar(32),
	"value"			varchar(256),

	CONSTRAINT MaintenanceParams_pk PRIMARY KEY(id),
    CONSTRAINT MaintenanceParams_fk FOREIGN KEY(reportid) references MaintenanceReport(id)
  );
  