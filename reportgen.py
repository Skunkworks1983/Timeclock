import argparse
from datetime import datetime, timedelta
import logging
import sqlite3

logging.basicConfig(level=logging.WARNING)
logger = logging.getLogger(__name__)

def main(args):
    conn = sqlite3.connect(args.dbfile)
    id = conn.execute('SELECT id FROM Members WHERE firstName=? AND lastName=?', (args.firstName,args.lastName,)).fetchone()[0]
    if id is None:
        logger.warning('Could not find ID')
        return
    cur = conn.execute('SELECT * from SignIns WHERE memberid=? ORDER BY time', (id,))

    intervals = []
    total = 0
    last_sign_in = None
    for _, timestamp, sign_in, is_force, session_id in cur.fetchall():
        timestamp = datetime.fromtimestamp(timestamp)
        if sign_in:
            last_sign_in = timestamp
            continue

        if last_sign_in is None:
            logger.warning('Sign out not preceded by a sign-in at %s', timestamp)
            continue

        duration = timestamp - last_sign_in
        if duration > timedelta(days=1):
            logger.warning('Found a sign-in sign-out pair longer than 24 hours: [%s]-[%s]', last_sign_in, timestamp)

        if is_force:
            duration = min(timedelta(hours=1), duration)
        intervals.append((
            last_sign_in,
            timestamp,
            duration.total_seconds() / 3600
        ))
        last_sign_in = None

    total = sum(duration for _, _, duration in intervals)
    with open(args.outfile, 'w') as f:
        f.write('Date,Sign in,Sign out,Hours\n')
        for start, end, duration in intervals:
            f.write(f'{start.date()},{start.time()},{end.time()},{duration:.2f}\n')

        f.write(f'Total,,{total:.2f}\n')



if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('dbfile')
    parser.add_argument('firstName')
    parser.add_argument('lastName')
    parser.add_argument('outfile')

    args = parser.parse_args()
    main(args)