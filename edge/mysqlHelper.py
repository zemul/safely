import MySQLdb
import config


class MysqlHelper(object):
    def __init__(self, charset='utf8'):
        self.host = config.MysqlHost
        self.port = config.MysqlPort
        self.db = config.MysqlDb
        self.user = config.MysqlUserName
        self.passwd = config.MysqlPassword
        self.charset = charset

    def connect(self):
        self.conn = MySQLdb.connect(host=self.host, port=self.port, db=self.db, user=self.user, passwd=self.passwd,
                                    charset=self.charset)
        self.cursor = self.conn.cursor()

    def close(self):
        self.cursor.close()
        self.conn.close()

    def get_one(self, sql, params=()):
        result = None
        try:
            self.connect()
            self.cursor.execute(sql, params)
            result = self.cursor.fetchone()
            self.close()
        except Exception as e:
            print(e)
        return result

    def get_all(self, sql, params=()):
        fetcchall = ()
        try:
            self.connect()
            self.cursor.execute(sql, params)
            fetcchall = self.cursor.fetchall()
            self.close()
        except Exception as e:
            print(e)
        return fetcchall

    def insert(self, sql, params=()):
        return self.__edit(sql, params)

    def update(self, sql, params=()):
        return self.__edit(sql, params)

    def delete(self, sql, params=()):
        return self.__edit(sql, params)

    def __edit(self, sql, params):
        count = 0
        try:
            self.connect()
            count = self.cursor.execute(sql, params)
            self.conn.commit()
            self.close()
        except Exception as e:
            print(e)
        
        return count
