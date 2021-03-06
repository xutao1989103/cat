package com.dianping.cat.report.task.cached;

import java.util.Date;
import java.util.Set;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.EventReportCountFilter;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixReportFilter;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionReportCountFilter;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.service.ReportServiceManager;

public class CachedReportTask implements Task {

	@Inject
	private ReportServiceManager m_reportService;

	@Inject
	private ServerConfigManager m_configManger;

	private MonthlyReport buildMonthlyReport(String domain, Date period, String name) {
		MonthlyReport report = new MonthlyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		return report;
	}

	private WeeklyReport buildWeeklyReport(String domain, Date period, String name) {
		WeeklyReport report = new WeeklyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		return report;
	}

	@Override
	public String getName() {
		return "Cached-Report-Task";
	}

	private void reloadCurrentMonthly() {
		Date start = TimeHelper.getCurrentMonth();
		Date end = TimeHelper.getCurrentDay();
		Set<String> domains = m_reportService.queryAllDomainNames(start, end, TransactionAnalyzer.ID);

		for (String domain : domains) {
			if (m_configManger.validateDomain(domain)) {
				Transaction t = Cat.newTransaction("ReloadTask", "Reload-Month-" + domain);

				TransactionReport transactionReport = m_reportService.queryTransactionReport(domain, start, end);
				new TransactionReportCountFilter().visitTransactionReport(transactionReport);

				m_reportService.insertMonthlyReport(buildMonthlyReport(domain, start, TransactionAnalyzer.ID),
				      com.dianping.cat.consumer.transaction.model.transform.DefaultNativeBuilder.build(transactionReport));

				EventReport eventReport = m_reportService.queryEventReport(domain, start, end);
				m_reportService.insertMonthlyReport(buildMonthlyReport(domain, start, EventAnalyzer.ID),
				      com.dianping.cat.consumer.event.model.transform.DefaultNativeBuilder.build(eventReport));

				ProblemReport problemReport = m_reportService.queryProblemReport(domain, start, end);
				m_reportService.insertMonthlyReport(buildMonthlyReport(domain, start, ProblemAnalyzer.ID),
				      com.dianping.cat.consumer.problem.model.transform.DefaultNativeBuilder.build(problemReport));

				CrossReport crossReport = m_reportService.queryCrossReport(domain, start, end);
				m_reportService.insertMonthlyReport(buildMonthlyReport(domain, start, CrossAnalyzer.ID),
				      com.dianping.cat.consumer.cross.model.transform.DefaultNativeBuilder.build(crossReport));

				MatrixReport matrixReport = m_reportService.queryMatrixReport(domain, start, end);
				new MatrixReportFilter().visitMatrixReport(matrixReport);
				m_reportService.insertMonthlyReport(buildMonthlyReport(domain, start, MatrixAnalyzer.ID),
				      com.dianping.cat.consumer.matrix.model.transform.DefaultNativeBuilder.build(matrixReport));

				t.setStatus(Transaction.SUCCESS);
				t.complete();
			}
		}
		String domain = Constants.CAT;

		StateReport stateReport = m_reportService.queryStateReport(domain, start, end);
		m_reportService.insertMonthlyReport(buildMonthlyReport(domain, start, StateAnalyzer.ID),
		      com.dianping.cat.consumer.state.model.transform.DefaultNativeBuilder.build(stateReport));
	}

	private void reloadCurrentWeekly() {
		Date start = TimeHelper.getCurrentWeek();
		Date end = TimeHelper.getCurrentDay();
		Set<String> domains = m_reportService.queryAllDomainNames(start, end, TransactionAnalyzer.ID);

		for (String domain : domains) {
			if (m_configManger.validateDomain(domain)) {
				Transaction t = Cat.newTransaction("ReloadTask", "Reload-Week-" + domain);

				TransactionReport transactionReport = m_reportService.queryTransactionReport(domain, start, end);
				new TransactionReportCountFilter().visitTransactionReport(transactionReport);

				m_reportService.insertWeeklyReport(buildWeeklyReport(domain, start, TransactionAnalyzer.ID),
				      com.dianping.cat.consumer.transaction.model.transform.DefaultNativeBuilder.build(transactionReport));

				EventReport eventReport = m_reportService.queryEventReport(domain, start, end);
				new EventReportCountFilter().visitEventReport(eventReport);

				m_reportService.insertWeeklyReport(buildWeeklyReport(domain, start, EventAnalyzer.ID),
				      com.dianping.cat.consumer.event.model.transform.DefaultNativeBuilder.build(eventReport));

				ProblemReport problemReport = m_reportService.queryProblemReport(domain, start, end);
				m_reportService.insertWeeklyReport(buildWeeklyReport(domain, start, ProblemAnalyzer.ID),
				      com.dianping.cat.consumer.problem.model.transform.DefaultNativeBuilder.build(problemReport));

				CrossReport crossReport = m_reportService.queryCrossReport(domain, start, end);
				m_reportService.insertWeeklyReport(buildWeeklyReport(domain, start, CrossAnalyzer.ID),
				      com.dianping.cat.consumer.cross.model.transform.DefaultNativeBuilder.build(crossReport));

				MatrixReport matrixReport = m_reportService.queryMatrixReport(domain, start, end);
				new MatrixReportFilter().visitMatrixReport(matrixReport);
				m_reportService.insertWeeklyReport(buildWeeklyReport(domain, start, MatrixAnalyzer.ID),
				      com.dianping.cat.consumer.matrix.model.transform.DefaultNativeBuilder.build(matrixReport));

				t.setStatus(Transaction.SUCCESS);
				t.complete();
			}
		}
		String domain = Constants.CAT;

		StateReport stateReport = m_reportService.queryStateReport(domain, start, end);
		m_reportService.insertWeeklyReport(buildWeeklyReport(domain, start, StateAnalyzer.ID),
		      com.dianping.cat.consumer.state.model.transform.DefaultNativeBuilder.build(stateReport));
	}

	@Override
	public void run() {
		reloadCurrentWeekly();
		reloadCurrentMonthly();
	}

	@Override
	public void shutdown() {
	}

}
