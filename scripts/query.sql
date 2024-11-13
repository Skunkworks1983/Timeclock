.headers on
.separator ','
SELECT
    concat(firstName, ' ', lastName) AS name,
    subteam,
    time AS signin,
    (
        SELECT time 
        FROM SignIns b
        WHERE a.memberId = b.memberId AND b.time > a.time
        ORDER BY time ASC
        LIMIT 1
    ) AS signout
FROM SignIns a
    INNER JOIN members
    ON memberId = id
WHERE
    role = 'STUDENT'
    AND signout - signin < 28800
;