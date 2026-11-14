package com.heritage.config;

import com.heritage.entity.DataSource;
import com.heritage.entity.FolkActivity;
import com.heritage.entity.Inheritor;
import com.heritage.entity.Project;
import com.heritage.entity.SysUser;
import com.heritage.repository.DataSourceRepository;
import com.heritage.repository.FolkActivityRepository;
import com.heritage.repository.InheritorRepository;
import com.heritage.repository.ProjectRepository;
import com.heritage.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 初始化数据：首次启动（集合为空）时写入管理员账号与演示数据，
 * 保证可以直接登录系统并看到统计图表效果。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    /** 演示用图片素材地址（仅存 URL 字符串） */
    private static final String IMAGE_URL =
            "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image"
                    + "?prompt=tibetan%20intangible%20cultural%20heritage%20traditional%20costume%20plateau"
                    + "&image_size=landscape_4_3";

    private final SysUserRepository sysUserRepository;
    private final ProjectRepository projectRepository;
    private final InheritorRepository inheritorRepository;
    private final FolkActivityRepository folkActivityRepository;
    private final DataSourceRepository dataSourceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initUsers();
        initHeritageData();
    }

    /** 初始化账号：admin / 123456（管理员）、user / 123456（普通用户） */
    private void initUsers() {
        if (sysUserRepository.count() > 0) {
            return;
        }
        sysUserRepository.save(buildUser("admin", "123456", "admin", "系统管理员"));
        sysUserRepository.save(buildUser("user", "123456", "user", "普通浏览用户"));
        log.info("初始化账号完成：admin/123456（管理员）、user/123456（普通用户）");
    }

    private SysUser buildUser(String account, String pwd, String role, String nickname) {
        SysUser user = new SysUser();
        user.setUserAccount(account);
        user.setUserPwd(passwordEncoder.encode(pwd));
        user.setUserRole(role);
        user.setNickname(nickname);
        return user;
    }

    /** 初始化非遗演示数据（含多对多 ID 引用与内嵌媒体数组） */
    private void initHeritageData() {
        if (projectRepository.count() > 0) {
            return;
        }

        // 数据来源
        DataSource s1 = saveSource("中国非物质文化遗产网", "https://www.ihchina.cn/", "国家级非遗名录官方发布平台");
        DataSource s2 = saveSource("西藏自治区文化厅", "http://wht.xizang.gov.cn/", "西藏自治区非遗普查资料");
        DataSource s3 = saveSource("青海省文化和旅游厅", "http://whhlyt.qinghai.gov.cn/", "青海省非遗保护成果汇编");

        // 传承人
        Inheritor i1 = saveInheritor("扎西顿珠", "男", "藏族", "藏族锅庄舞代表性传承人，长期从事锅庄舞教学与展演");
        Inheritor i2 = saveInheritor("次仁旺姆", "女", "藏族", "藏戏（蓝面具）传承人，主持民间藏戏队演出");
        Inheritor i3 = saveInheritor("更登达吉", "男", "藏族", "热贡唐卡绘制技艺传承人，作品多次赴内地展出");
        Inheritor i4 = saveInheritor("尼玛才让", "男", "藏族", "藏医药（七十味珍珠丸配伍技艺）传承人");
        Inheritor i5 = saveInheritor("索南扎西", "男", "藏族", "藏族碉楼营造技艺传承人，掌握石砌墙体收分工艺");

        // 民俗活动
        FolkActivity a1 = saveActivity("玉树赛马会", "青海省玉树藏族自治州", "每年7月下旬", "集赛马、歌舞、服饰展示于一体的传统民俗盛会");
        FolkActivity a2 = saveActivity("藏历新年", "西藏拉萨", "藏历正月初一", "藏族最隆重的传统节日，含煨桑、跳锅庄等民俗活动");
        FolkActivity a3 = saveActivity("雪顿节", "西藏拉萨", "藏历六月三十", "以藏戏展演和酸奶宴为核心的民族文化节日");
        FolkActivity a4 = saveActivity("望果节", "西藏山南", "每年秋收前", "庆祝丰收、祈求风调雨顺的农业民俗活动");

        // 非遗项目（含关联引用与内嵌 media_list）
        saveProject("藏族锅庄舞", "传统舞蹈", "国家级", "西藏拉萨", s1, i1, a2,
                "锅庄舞是藏族群众自娱性圆圈歌舞，动作粗犷豪放，节奏鲜明。",
                "活态传承良好，社区传习点覆盖城乡");
        saveProject("藏戏", "传统戏剧", "国家级", "西藏拉萨", s1, i2, a3,
                "藏戏被誉为藏文化的活化石，以唱、舞、韵、白、表、技综合表演为特征。",
                "专业剧团与民间藏戏队并存");
        saveProject("热贡唐卡", "传统美术", "国家级", "青海玉树", s3, i3, a4,
                "热贡唐卡以矿物颜料绘制，色彩浓艳、线条精细，具有鲜明地域风格。",
                "传承人群稳定增长，产业化初具规模");
        saveProject("藏医药", "传统医药", "国家级", "西藏拉萨", s2, i4, a2,
                "藏医药体系完整，对高原常见病诊疗具有独特疗效。",
                "藏医医疗机构与传承体系健全");
        saveProject("藏族碉楼营造技艺", "传统技艺", "省级", "西藏山南", s2, i5, a4,
                "碉楼以片石砌筑，墙体收分明显，是高原聚落建筑的典型代表。",
                "掌握核心技艺的匠人数量偏少");
        saveProject("玉树赛马会", "民俗", "国家级", "青海玉树", s3, i1, a1,
                "赛马会是康巴藏区规模最大的传统民俗活动，兼具竞技与展演属性。",
                "年度常态化举办");
        saveProject("藏历民俗节庆", "民俗", "省级", "青海玉树", s3, i2, a1,
                "以藏历新年、望果节等为代表的高原节庆民俗集合。",
                "群众参与度高，节庆活动活跃");

        log.info("初始化非遗演示数据完成");
    }

    private DataSource saveSource(String name, String url, String note) {
        DataSource source = new DataSource();
        source.setSourceName(name);
        source.setOriginalUrl(url);
        source.setSourceNote(note);
        return dataSourceRepository.save(source);
    }

    private Inheritor saveInheritor(String name, String gender, String nation, String intro) {
        Inheritor inheritor = new Inheritor();
        inheritor.setInheritorName(name);
        inheritor.setGender(gender);
        inheritor.setNation(nation);
        inheritor.setIntro(intro);
        return inheritorRepository.save(inheritor);
    }

    private FolkActivity saveActivity(String name, String addr, String time, String desc) {
        FolkActivity activity = new FolkActivity();
        activity.setActivityName(name);
        activity.setActivityAddr(addr);
        activity.setActivityTime(time);
        activity.setActivityDesc(desc);
        return folkActivityRepository.save(activity);
    }

    private void saveProject(String name, String category, String level, String region,
                             DataSource source, Inheritor inheritor, FolkActivity activity,
                             String intro, String inheritStatus) {
        Project project = new Project();
        project.setProjectName(name);
        project.setProjectCategory(category);
        project.setProjectLevel(level);
        project.setRegion(region);
        project.setProjectIntro(intro);
        project.setInheritStatus(inheritStatus);
        project.setSourceRef(source.getId());
        project.setInheritorRefList(List.of(inheritor.getId()));
        project.setActivityRefList(List.of(activity.getId()));
        // 内嵌多媒体素材：只保存 URL 字符串（图片 / 视频），不存储二进制
        project.setMediaList(List.of(
                new Project.MediaItem("图片", IMAGE_URL, name + "展示图片"),
                new Project.MediaItem("视频", "https://www.ihchina.cn/video/demo.mp4", name + "记录视频")));
        project.setCreateTime(LocalDateTime.now());
        projectRepository.save(project);
    }
}