package com.timgroup.tucker.info.status;

import com.timgroup.tucker.info.Component;

public interface ApplicationReportGenerator {
    Component getVersionComponent();
    StatusPage getApplicationReport();
}