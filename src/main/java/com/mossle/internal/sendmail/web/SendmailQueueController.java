package com.mossle.internal.sendmail.web;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mossle.core.hibernate.PropertyFilter;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.spring.MessageHelper;
import com.mossle.core.util.StringUtils;

import com.mossle.ext.export.Exportor;
import com.mossle.ext.export.TableModel;
import com.mossle.ext.mail.MailDTO;
import com.mossle.ext.mail.MailHelper;
import com.mossle.ext.mail.MailServerInfo;

import com.mossle.internal.sendmail.persistence.domain.SendmailConfig;
import com.mossle.internal.sendmail.persistence.domain.SendmailQueue;
import com.mossle.internal.sendmail.persistence.domain.SendmailTemplate;
import com.mossle.internal.sendmail.persistence.manager.SendmailConfigManager;
import com.mossle.internal.sendmail.persistence.manager.SendmailQueueManager;
import com.mossle.internal.sendmail.persistence.manager.SendmailTemplateManager;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sendmail")
public class SendmailQueueController {
    private SendmailQueueManager sendmailQueueManager;
    private SendmailConfigManager sendmailConfigManager;
    private SendmailTemplateManager sendmailTemplateManager;
    private MessageHelper messageHelper;
    private Exportor exportor;
    private BeanMapper beanMapper = new BeanMapper();

    @RequestMapping("sendmail-queue-list")
    public String list(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap, Model model) {
        page.setDefaultOrder("createTime", Page.DESC);

        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        page = sendmailQueueManager.pagedQuery(page, propertyFilters);
        model.addAttribute("page", page);

        return "sendmail/sendmail-queue-list";
    }

    @RequestMapping("sendmail-queue-input")
    public String input(@RequestParam(value = "id", required = false) Long id,
            Model model) {
        if (id != null) {
            SendmailQueue sendmailQueue = sendmailQueueManager.get(id);
            model.addAttribute("model", sendmailQueue);
        }

        model.addAttribute("sendmailConfigs", sendmailConfigManager.getAll());
        model.addAttribute("sendmailTemplates",
                sendmailTemplateManager.getAll());

        return "sendmail/sendmail-queue-input";
    }

    @RequestMapping("sendmail-queue-save")
    public String save(@ModelAttribute SendmailQueue sendmailQueue,
            @RequestParam("sendmailConfigId") Long sendmailConfigId,
            @RequestParam("sendmailTemplateId") Long sendmailTemplateId,
            RedirectAttributes redirectAttributes) {
        Long id = sendmailQueue.getId();
        SendmailQueue dest = null;

        if (id != null) {
            dest = sendmailQueueManager.get(id);
            beanMapper.copy(sendmailQueue, dest);
        } else {
            dest = sendmailQueue;
            dest.setCreateTime(new Date());
        }

        SendmailTemplate sendmailTemplate = sendmailTemplateManager
                .get(sendmailTemplateId);
        dest.setSendmailConfig(sendmailConfigManager.get(sendmailConfigId));
        dest.setSendmailTemplate(sendmailTemplate);

        sendmailQueueManager.save(dest);
        messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
                "保存成功");

        return "redirect:/sendmail/sendmail-queue-list.do";
    }

    @RequestMapping("sendmail-queue-remove")
    public String remove(@RequestParam("selectedItem") List<Long> selectedItem,
            RedirectAttributes redirectAttributes) {
        List<SendmailQueue> sendmailQueues = sendmailQueueManager
                .findByIds(selectedItem);
        sendmailQueueManager.removeAll(sendmailQueues);
        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.delete", "删除成功");

        return "redirect:/sendmail/sendmail-queue-list.do";
    }

    @RequestMapping("sendmail-queue-export")
    public void export(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        page = sendmailQueueManager.pagedQuery(page, propertyFilters);

        List<SendmailQueue> sendmailQueues = (List<SendmailQueue>) page
                .getResult();

        TableModel tableModel = new TableModel();
        tableModel.setName("mail config");
        tableModel.addHeaders("id", "name");
        tableModel.setData(sendmailQueues);
        exportor.export(request, response, tableModel);
    }

    // ~ ======================================================================
    @Resource
    public void setSendmailQueueManager(
            SendmailQueueManager sendmailQueueManager) {
        this.sendmailQueueManager = sendmailQueueManager;
    }

    @Resource
    public void setSendmailConfigManager(
            SendmailConfigManager sendmailConfigManager) {
        this.sendmailConfigManager = sendmailConfigManager;
    }

    @Resource
    public void setSendmailTemplateManager(
            SendmailTemplateManager sendmailTemplateManager) {
        this.sendmailTemplateManager = sendmailTemplateManager;
    }

    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    @Resource
    public void setExportor(Exportor exportor) {
        this.exportor = exportor;
    }
}
