package com.im.qtech.chk;

import com.im.aa.inspection.entity.standard.EqLstTplDO;
import com.im.aa.inspection.entity.standard.EqLstTplInfoPO;
import com.im.aa.inspection.service.DatabaseService;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestMain {

    public static void main(String[] args) {
        // ✅ 1. 初始化 DatabaseService
        DatabaseService databaseService = new DatabaseService();
        System.out.println("✅ DatabaseService 初始化成功");

        // ✅ 2. 获取 Session
        Session session = databaseService.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            // ✅ 3. 测试插入 EqLstTplInfoPO
            EqLstTplInfoPO infoPO = new EqLstTplInfoPO();
            infoPO.setModule("MODULE_TEST");
            infoPO.setProvider("PROVIDER_TEST");

            session.saveOrUpdate(infoPO);
            System.out.println("✅ 插入 EqLstTplInfoPO 成功，ID = " + infoPO.getId());

            // ✅ 4. 测试插入 EqLstTplDO
            EqLstTplDO tplDO = new EqLstTplDO();
            tplDO.setModule("MODULE_TEST");
            tplDO.setRemark("模板内容示例");

            session.saveOrUpdate(tplDO);
            System.out.println("✅ 插入 EqLstTplDO 成功，ID = " + tplDO.getId());


            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }

        // ✅ 6. 测试查询
        try {
            EqLstTplInfoPO loaded = databaseService.getTplInfo("MODULE_TEST");
            if (loaded != null) {
                System.out.println("✅ findByModule 查询成功 -> " + loaded.getModule());
            } else {
                System.out.println("❌ findByModule 返回 null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ✅ 7. 关闭工厂
        databaseService.close();
        System.out.println("✅ 测试结束");
    }
}
