CREATE TABLE MaintenanceReport
  (
    id         varchar(256),
	taskClass  varchar(64),
    category   int,
	severity   int,
	ignored    char DEFAULT 'n',
    primary key(id)
  );
  
CREATE TABLE MaintenanceReportLocalization 
  (
    reportid        int,
    langid			varchar(3),
	name            varchar(32)   not null,
    description     varchar(1048),
	
	primary key (reportId, langid),
	
    foreign key(reportid) references MaintenanceReport(id)
  );

 
CREATE TABLE MaintenanceParams
  (
    reportid        int,
	id				int auto_increment,
	name			varchar(32),
	"value"			varchar(256),
	
	primary key (reportId, id),
	
    foreign key(reportid) references MaintenanceReport(id)
  );
  