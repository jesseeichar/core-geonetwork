CREATE TABLE MaintenanceReport
  (
    id         varchar(256),
	taskClass  varchar(64),
    category   integer NOT NULL,
	severity   integer NOT NULL,
	ignored    CHAR(1) DEFAULT 'n',
	
	CONSTRAINT MaintenanceReport_pk PRIMARY KEY(id)
  );
  
CREATE TABLE MaintenanceReportLocalization 
  (
    reportid        integer,
    langid			varchar(3) NOT NULL,
	name            varchar(32) NOT NULL,
    description     varchar(1048),

	CONSTRAINT MaintenanceReportLocal_pk PRIMARY KEY(reportId, langid),
    CONSTRAINT MaintenanceReportLocal_fk FOREIGN KEY(reportid) references MaintenanceReport(id)
  );
 
CREATE TABLE MaintenanceParams
  (
    reportid        integer,
	id				SERIAL,
	name			varchar(32),
	"value"			varchar(256),

	CONSTRAINT MaintenanceParams_pk PRIMARY KEY(id),
    CONSTRAINT MaintenanceParams_fk FOREIGN KEY(reportid) references MaintenanceReport(id)
  );
  