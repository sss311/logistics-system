package com.logistics;

import com.logistics.mapper.ParcelMapper;
import com.logistics.mapper.WaybillMapper;
import com.logistics.model.Parcel;
import com.logistics.model.Waybill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestAutoAdvance implements CommandLineRunner {

    @Autowired
    private ParcelMapper parcelMapper;

    @Autowired
    private WaybillMapper waybillMapper;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("========== 开始初始化测试包裹 ==========");

        // 1. 检查是否已有包裹，如果有则跳过初始化
        List<Parcel> existingParcels = parcelMapper.selectAll();
        if (!existingParcels.isEmpty()) {
            System.out.println("数据库已有 " + existingParcels.size() + " 个包裹，跳过初始化");
            printAllParcels();
            return;
        }

        // 2. 创建测试包裹
        int now = (int) (System.currentTimeMillis() / 1000);

        // 包裹A：待揽收（新鲜创建，还没揽收）
        Parcel parcelA = new Parcel();
        parcelA.setId("P_TEST_A");
        parcelA.setSender("CUS_GOLD");
        parcelA.setReceiver("CUS_SILVER");
        parcelA.setCategory("普通");
        parcelA.setDeclaredValue(1000);
        parcelA.setPriority("普通");
        parcelA.setCod(true);
        parcelA.setCodAmount(500);
        parcelA.setAddress("北京市朝阳区测试点A");
        parcelA.setStatus("待揽收");
        parcelA.setCreatedAt(now);
        parcelA.setFailCount(0);
        parcelA.setLastStationTime(0);
        parcelA.setLastStationClock(0);
        parcelMapper.insert(parcelA);
        System.out.println("创建包裹A: P_TEST_A (待揽收)");

        // 包裹B：待派送（走完揽收+分拣+推进，停在ST_DELIV_A等待派单）
        Parcel parcelB = new Parcel();
        parcelB.setId("P_TEST_B");
        parcelB.setSender("CUS_GOLD");
        parcelB.setReceiver("CUS_SILVER");
        parcelB.setCategory("普通");
        parcelB.setDeclaredValue(800);
        parcelB.setPriority("加急");
        parcelB.setCod(false);
        parcelB.setCodAmount(0);
        parcelB.setAddress("北京市朝阳区测试点B");
        parcelB.setStatus("待派送");
        parcelB.setCurrentStation("ST_DELIV_A");
        parcelB.setZone("C");
        parcelB.setCreatedAt(now);
        parcelB.setFailCount(0);
        parcelB.setLastStationTime(0);
        parcelB.setLastStationClock(0);
        parcelMapper.insert(parcelB);
        System.out.println("创建包裹B: P_TEST_B (待派送)");

        // 包裹C：派送中（已分配快递员CR_1，正在派送）
        Parcel parcelC = new Parcel();
        parcelC.setId("P_TEST_C");
        parcelC.setSender("CUS_GOLD");
        parcelC.setReceiver("CUS_BRONZE");
        parcelC.setCategory("易碎");
        parcelC.setDeclaredValue(2000);
        parcelC.setPriority("普通");
        parcelC.setCod(true);
        parcelC.setCodAmount(800);
        parcelC.setAddress("北京市海淀区测试点C");
        parcelC.setStatus("派送中");
        parcelC.setCurrentStation("ST_DELIV_A");
        parcelC.setZone("A");
        parcelC.setCourierId("CR_1");
        parcelC.setCreatedAt(now);
        parcelC.setFailCount(0);
        parcelC.setLastStationTime(0);
        parcelC.setLastStationClock(0);
        parcelMapper.insert(parcelC);
        System.out.println("创建包裹C: P_TEST_C (派送中，快递员CR_1)");

        System.out.println("========== 初始化完成，共创建3个测试包裹 ==========");
        printAllParcels();
    }

    /**
     * 打印所有包裹的关键信息
     */
    private void printAllParcels() {
        System.out.println("========== 系统状态检查 ==========");
        List<Parcel> parcels = parcelMapper.selectAll();
        if (parcels.isEmpty()) {
            System.out.println("暂无包裹数据");
        } else {
            for (Parcel parcel : parcels) {
                System.out.println("包裹ID: " + parcel.getId());
                System.out.println("  状态: " + parcel.getStatus());
                System.out.println("  当前位置: " + parcel.getCurrentStation());
                System.out.println("  最后更新时钟: " + parcel.getLastStationClock());
                System.out.println("  失败次数: " + parcel.getFailCount());
                System.out.println("  快递员ID: " + parcel.getCourierId());

                Waybill waybill = waybillMapper.selectByParcelId(parcel.getId());
                if (waybill != null) {
                    System.out.println("  运单ID: " + waybill.getId());
                    System.out.println("  运单状态: " + waybill.getStatus());
                    System.out.println("  当前站点: " + waybill.getCurrentStation());
                    System.out.println("  路由段: " + waybill.getRouteLegs());
                } else {
                    System.out.println("  运单: 无");
                }
                System.out.println("---");
            }
        }
        System.out.println("========== 检查完成 ==========");
    }
}