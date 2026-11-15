package com.bishe.web.service.followup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FollowupPlanScheduleInitializer implements ApplicationRunner
{
    private static final Logger log = LoggerFactory.getLogger(FollowupPlanScheduleInitializer.class);

    private final FollowupPlanScheduleService followupPlanScheduleService;

    public FollowupPlanScheduleInitializer(FollowupPlanScheduleService followupPlanScheduleService)
    {
        this.followupPlanScheduleService = followupPlanScheduleService;
    }

    @Override
    public void run(ApplicationArguments args)
    {
        try
        {
            followupPlanScheduleService.syncAllPlansOnStartup();
        }
        catch (Exception ex)
        {
            log.warn("Skip followup plan schedule initialization because the database schema is not ready.", ex);
        }
    }
}
