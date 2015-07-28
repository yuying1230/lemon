package com.mossle.internal.sendmail.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;

import com.mossle.internal.sendmail.persistence.domain.SendmailTemplate;

import org.springframework.stereotype.Service;

@Service
public class SendmailTemplateManager extends
        HibernateEntityDao<SendmailTemplate> {
}
