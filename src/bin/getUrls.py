#!/usr/bin/python
# -*- coding:UTF-8 -*-
import MySQLdb
import sys

try:
    conn=MySQLdb.connect(host='database1.imcle.com',user='search',passwd='P@ssw0rd',db='TTROS_GroupMng',port=3306)  
    sql='SELECT DISTINCT UrlHost FROM ENT_NetMedia, ENT_Enterprise, ETA_ETradeArea, ETA_ETradeAreaEnterprise,       ETA_ETradeAreaEnterpriseNetMedia WHERE ENT_NetMedia.EnterpriseID = ENT_Enterprise.ID AND ENT_Enterprise.ID = ETA_ETradeAreaEnterprise.EnterpriseID AND ETA_ETradeAreaEnterprise.ETradeAreaID = ETA_ETradeArea.ID AND ETA_ETradeAreaEnterpriseNetMedia.ETradeAreaEnterpriseID = ETA_ETradeAreaEnterprise.ID AND ETA_ETradeAreaEnterpriseNetMedia.NetMediaID = ENT_NetMedia.ID AND ENT_NetMedia.STATUS = 1 AND ENT_Enterprise.STATUS & 5 = 1 AND ETA_ETradeArea. STATUS & 7 =3'
    cur=conn.cursor()
    count=cur.execute(sql)
    print 'there has %s rows record' % count

    results=cur.fetchall()
    f = file('./urlsTmp.txt','w+')
    for r in results:
        f.write("http://"+r[0]+"\n")
    f.read()
    f.close()
    cur.close()
    conn.close()
    sys.exit(0)
except MySQLdb.Error,e:
     print "Mysql Error %d: %s" % (e.args[0], e.args[1]);