package com.bishe.system.service.impl;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.bishe.common.utils.DateUtils;
import com.bishe.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bishe.system.domain.HealthArchive;
import com.bishe.system.domain.HealthWarning;
import com.bishe.system.mapper.HealthArchiveMapper;
import com.bishe.system.service.IHealthWarningService;
import com.bishe.system.service.IHealthArchiveService;

/**
 * 电子健康档案Service业务层处理
 * 
 * @author bishe
 * @date 2025-05-22
 */
@Service
public class HealthArchiveServiceImpl implements IHealthArchiveService 
{
    private static final Pattern AGE_PATTERN = Pattern.compile("(\\d{1,3})");

    @Autowired
    private HealthArchiveMapper healthArchiveMapper;

    @Autowired
    private IHealthWarningService healthWarningService;

    /**
     * 查询电子健康档案
     * 
     * @param archiveId 电子健康档案主键
     * @return 电子健康档案
     */
    @Override
    public HealthArchive selectHealthArchiveById(Long archiveId)
    {
        return enrichArchive(healthArchiveMapper.selectHealthArchiveById(archiveId));
    }

    /**
     * 查询电子健康档案列表
     * 
     * @param healthArchive 电子健康档案
     * @return 电子健康档案
     */
    @Override
    public List<HealthArchive> selectHealthArchiveList(HealthArchive healthArchive)
    {
        List<HealthArchive> list = healthArchiveMapper.selectHealthArchiveList(healthArchive);
        list.forEach(this::enrichArchive);
        return list;
    }

    /**
     * 新增电子健康档案
     * 
     * @param healthArchive 电子健康档案
     * @return 结果
     */
    @Override
    public int insertHealthArchive(HealthArchive healthArchive)
    {
        prepareArchive(healthArchive);
        healthArchive.setCreateTime(DateUtils.getNowDate());
        return healthArchiveMapper.insertHealthArchive(healthArchive);
    }

    /**
     * 修改电子健康档案
     * 
     * @param healthArchive 电子健康档案
     * @return 结果
     */
    @Override
    public int updateHealthArchive(HealthArchive healthArchive)
    {
        prepareArchive(healthArchive);
        healthArchive.setUpdateTime(DateUtils.getNowDate());
        return healthArchiveMapper.updateHealthArchive(healthArchive);
    }

    /**
     * 批量删除电子健康档案
     * 
     * @param archiveIds 需要删除的电子健康档案主键
     * @return 结果
     */
    @Override
    public int deleteHealthArchiveByIds(Long[] archiveIds)
    {
        return healthArchiveMapper.deleteHealthArchiveByIds(archiveIds);
    }

    /**
     * 删除电子健康档案信息
     * 
     * @param archiveId 电子健康档案主键
     * @return 结果
     */
    @Override
    public int deleteHealthArchiveById(Long archiveId)
    {
        return healthArchiveMapper.deleteHealthArchiveById(archiveId);
    }

    private HealthArchive enrichArchive(HealthArchive archive)
    {
        if (archive == null)
        {
            return null;
        }
        if (StringUtils.isEmpty(archive.getDiseaseType()))
        {
            archive.setDiseaseType(deriveDiseaseType(archive));
        }
        if (StringUtils.isEmpty(archive.getAgeStage()))
        {
            archive.setAgeStage(deriveAgeStage(archive));
        }
        if (StringUtils.isEmpty(archive.getRiskLevel()))
        {
            archive.setRiskLevel(deriveRiskLevel(archive.getUserId()));
        }
        if (StringUtils.isEmpty(archive.getArchiveTags()))
        {
            archive.setArchiveTags(buildArchiveTags(archive));
        }
        return archive;
    }

    private void prepareArchive(HealthArchive archive)
    {
        if (archive == null)
        {
            return;
        }
        archive.setDiseaseType(StringUtils.trimToNull(archive.getDiseaseType()));
        archive.setAgeStage(StringUtils.trimToNull(archive.getAgeStage()));
        archive.setRiskLevel(StringUtils.trimToNull(archive.getRiskLevel()));
        archive.setArchiveTags(StringUtils.trimToNull(archive.getArchiveTags()));

        if (StringUtils.isEmpty(archive.getDiseaseType()))
        {
            archive.setDiseaseType(deriveDiseaseType(archive));
        }
        if (StringUtils.isEmpty(archive.getAgeStage()))
        {
            archive.setAgeStage(deriveAgeStage(archive));
        }
        if (StringUtils.isEmpty(archive.getRiskLevel()))
        {
            archive.setRiskLevel(deriveRiskLevel(archive.getUserId()));
        }
        if (StringUtils.isEmpty(archive.getArchiveTags()))
        {
            archive.setArchiveTags(buildArchiveTags(archive));
        }
    }

    private String deriveDiseaseType(HealthArchive archive)
    {
        String source = StringUtils.defaultString(archive.getDiseaseType())
            + StringUtils.defaultString(archive.getMedicalHistory())
            + StringUtils.defaultString(archive.getDiagnosisRecord())
            + StringUtils.defaultString(archive.getTrainingPlan())
            + StringUtils.defaultString(archive.getMedicationAdvice())
            + StringUtils.defaultString(archive.getRemark());

        if (source.contains("高血压") || source.contains("血压"))
        {
            return "高血压";
        }
        if (source.contains("糖尿病") || source.contains("血糖"))
        {
            return "糖尿病";
        }
        if (source.contains("冠心病") || source.contains("心脏"))
        {
            return "冠心病";
        }
        if (source.contains("脑卒中") || source.contains("脑梗") || source.contains("脑出血"))
        {
            return "脑卒中";
        }
        if (source.contains("慢阻肺") || source.contains("肺"))
        {
            return "慢阻肺";
        }
        if (source.contains("康复") || source.contains("训练") || source.contains("骨"))
        {
            return "骨科康复";
        }
        return "慢病管理";
    }

    private String deriveAgeStage(HealthArchive archive)
    {
        Integer age = extractAge(archive);
        if (age != null)
        {
            if (age >= 75)
            {
                return "高龄";
            }
            if (age >= 60)
            {
                return "老年";
            }
            if (age >= 45)
            {
                return "中年";
            }
            return "青年";
        }

        String source = StringUtils.defaultString(archive.getBasicInfo())
            + StringUtils.defaultString(archive.getMedicalHistory())
            + StringUtils.defaultString(archive.getRemark());
        if (source.contains("高龄"))
        {
            return "高龄";
        }
        if (source.contains("老年"))
        {
            return "老年";
        }
        if (source.contains("青年"))
        {
            return "青年";
        }
        if (source.contains("中年"))
        {
            return "中年";
        }
        return "成人";
    }

    private Integer extractAge(HealthArchive archive)
    {
        String basicInfo = StringUtils.defaultString(archive.getBasicInfo());
        if (StringUtils.isEmpty(basicInfo))
        {
            return null;
        }

        Integer directAge = parseAgeValue(extractJsonValue(basicInfo, "age"));
        if (directAge != null)
        {
            return directAge;
        }

        String birthDate = extractJsonValue(basicInfo, "birthDate");
        if (StringUtils.isEmpty(birthDate))
        {
            birthDate = extractJsonValue(basicInfo, "birthday");
        }
        if (StringUtils.isNotEmpty(birthDate))
        {
            Integer age = parseAgeFromBirthDate(birthDate);
            if (age != null)
            {
                return age;
            }
        }
        return null;
    }

    private Integer parseAgeValue(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return null;
        }
        Matcher matcher = AGE_PATTERN.matcher(value);
        if (matcher.find())
        {
            int age = Integer.parseInt(matcher.group(1));
            if (age >= 0 && age <= 120)
            {
                return age;
            }
        }
        return null;
    }

    private Integer parseAgeFromBirthDate(String birthDate)
    {
        String normalized = birthDate.trim().replace('.', '-').replace('/', '-');
        for (DateTimeFormatter formatter : new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-M-d"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        })
        {
            try
            {
                LocalDate birth = LocalDate.parse(normalized, formatter);
                int age = Period.between(birth, LocalDate.now()).getYears();
                return age >= 0 && age <= 120 ? age : null;
            }
            catch (DateTimeParseException ignored)
            {
            }
        }
        return null;
    }

    private String extractJsonValue(String json, String key)
    {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start < 0)
        {
            return null;
        }
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end < 0)
        {
            return null;
        }
        return json.substring(start, end);
    }

    private String deriveRiskLevel(Long userId)
    {
        if (userId == null)
        {
            return "低风险";
        }
        HealthWarning query = new HealthWarning();
        query.setUserId(userId);
        List<HealthWarning> warnings = healthWarningService.selectHealthWarningList(query);
        boolean hasLevel1 = false;
        boolean hasLevel2 = false;
        for (HealthWarning warning : warnings)
        {
            if ("3".equals(warning.getWarningLevel()))
            {
                return "高风险";
            }
            if ("2".equals(warning.getWarningLevel()))
            {
                hasLevel2 = true;
            }
            else if ("1".equals(warning.getWarningLevel()))
            {
                hasLevel1 = true;
            }
        }
        if (hasLevel2)
        {
            return "中风险";
        }
        if (hasLevel1)
        {
            return "低风险";
        }
        return "低风险";
    }

    private String buildArchiveTags(HealthArchive archive)
    {
        StringBuilder builder = new StringBuilder();
        appendTag(builder, archive.getDiseaseType());
        appendTag(builder, archive.getAgeStage());
        appendTag(builder, archive.getRiskLevel());
        if (StringUtils.isNotEmpty(archive.getTrainingPlan()))
        {
            appendTag(builder, "康复训练");
        }
        return builder.toString();
    }

    private void appendTag(StringBuilder builder, String tag)
    {
        if (StringUtils.isEmpty(tag))
        {
            return;
        }
        if (builder.length() > 0)
        {
            builder.append(",");
        }
        builder.append(tag);
    }
}
