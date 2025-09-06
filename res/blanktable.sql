CREATE TABLE members(
id VARCHAR(36) PRIMARY KEY,
role VARCHAR,
firstName VARCHAR,
lastName VARCHAR,
hours REAL,
isSignedIn INTEGER
, lastSignedIn BIGINT, penalties INTEGER);
CREATE TABLE pins(
memberId VARCHAR(36) PRIMARY KEY,
salt BLOB(4),
hash VARCHAR,
FOREIGN KEY(memberId) REFERENCES members(id)
);
CREATE TABLE sessions(
id VARCHAR(36) PRIMARY KEY,
scheduledHours REAL,
startedBy VARCHAR(36),
endedBy VARCHAR(36), start BIGINT, end BIGINT,
FOREIGN KEY(startedBy) REFERENCES members(id),
FOREIGN KEY(endedBy) REFERENCES members(id)
);
CREATE TABLE IF NOT EXISTS "SignIns" (
memberId VARCHAR(36),
time BIGINT,
isSigningIn INTEGER,
isForce INTEGER,
sessionId VARCHAR(36),
PRIMARY KEY (memberId, time));
CREATE TABLE MemberTransactions (
time BIGINT,
tableName VARCHAR(20),
id VARCHAR(36),
PRIMARY KEY (time, tableName, id));