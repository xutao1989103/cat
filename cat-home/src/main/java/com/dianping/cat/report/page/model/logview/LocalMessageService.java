package com.dianping.cat.report.page.model.logview;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.dianping.cat.storage.message.LocalMessageBucketManager;
import com.dianping.cat.storage.message.MessageBucketManager;

public class LocalMessageService extends BaseLocalModelService<String> {
	@Inject(LocalMessageBucketManager.ID)
	private MessageBucketManager m_bucketManager;

	@Inject("html")
	private MessageCodec m_codec;

	public LocalMessageService() {
		super("logview");
	}

	@Override
	protected String getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		String messageId = request.getProperty("messageId");

		if (messageId == null) {
			return null;
		}

		MessageTree tree = m_bucketManager.loadMessage(messageId);

		if (tree != null) {
			ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);

			if (tree.getMessage() instanceof Transaction && request.getProperty("waterfall", "false").equals("true")) {
				// to work around a plexus injection bug
				MessageCodec codec = lookup(MessageCodec.class, "waterfall");

				codec.encode(tree, buf);
			} else {
				m_codec.encode(tree, buf);
			}

			try {
				buf.readInt(); // get rid of length
				return buf.toString(Charset.forName("utf-8"));
			} catch (Exception e) {
				// ignore it
			}
		}
		return null;
	}

	@Override
	public ModelResponse<String> invoke(ModelRequest request) {
		ModelResponse<String> response = new ModelResponse<String>();
		Transaction t = Cat.newTransaction("ModelService", getClass().getSimpleName());

		try {
			ModelPeriod period = request.getPeriod();
			String domain = request.getDomain();
			String report = getReport(request, period, domain);

			response.setModel(report);

			t.addData("period", period);
			t.addData("domain", domain);
			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			Cat.logError(e);
			t.setStatus(e);
			response.setException(e);
		} finally {
			t.complete();
		}
		return response;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		boolean eligibale = request.getPeriod().isCurrent();

		if (eligibale) {
			String messageId = request.getProperty("messageId");
			MessageId id = MessageId.parse(messageId);

			return id.getVersion() == 2;
		}

		return eligibale;
	}
}
