
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import cn.edu.hfut.dmic.webcollector.util.Config;
import cn.edu.hfut.dmic.webcollector.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * WebCollectorץȡͼƬ������
 */
public class DemoImageCrawler extends BreadthCrawler {

	// ���ڱ���ͼƬ���ļ���
	File downloadDir;

	// ԭ����int����������ͼƬ�ļ���
	AtomicInteger imageId;

	/**
	 * 
	 * @param crawlPath
	 *            ����ά��URL���ļ���
	 * @param downloadPath
	 *            ���ڱ���ͼƬ���ļ���
	 */
	public DemoImageCrawler(String crawlPath, String downloadPath) {
		super(crawlPath, true);
		downloadDir = new File(downloadPath);
		if (!downloadDir.exists()) {
			downloadDir.mkdirs();
		}
		computeImageId();
	}

	@Override
	public void visit(Page page, CrawlDatums next) {
		// ����httpͷ�е�Content-Type��Ϣ���жϵ�ǰ��Դ����ҳ����ͼƬ
		String contentType = page.getResponse().getContentType();
		if (contentType == null) {
			return;
		} else if (contentType.contains("html")) {
			// �������ҳ�����ȡ���а���ͼƬ��URL�������������
			Elements imgs = page.select("img[src]");
			for (Element img : imgs) {
				String imgSrc = img.attr("abs:src");
				next.add(imgSrc);
			}

		} else if (contentType.startsWith("image")) {
			// �����ͼƬ��ֱ������
			String extensionName = contentType.split("/")[1];
			String imageFileName = imageId.incrementAndGet() + "." + extensionName;
			File imageFile = new File(downloadDir, imageFileName);
			try {
				FileUtils.writeFile(imageFile, page.getContent());
				System.out.println("����ͼƬ " + page.getUrl() + " �� " + imageFile.getAbsolutePath());
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}

	}

	public static void main(String[] args) throws Exception {
		DemoImageCrawler demoImageCrawler = new DemoImageCrawler("crawl", "download3");
		// �������URL
		demoImageCrawler.addSeed("https://www.starbucks.com.cn/");
		// �޶���ȡ��Χ
		demoImageCrawler.addRegex("https://www.starbucks.com.cn/.*");
		// ����Ϊ�ϵ���ȡ������ÿ�ο������涼��������ȡ
		demoImageCrawler.setResumable(true);
		demoImageCrawler.setThreads(30);
		Config.MAX_RECEIVE_SIZE = 1000 * 1000 * 10;
		demoImageCrawler.start(3);
	}

	public void computeImageId() {
		int maxId = -1;
		for (File imageFile : downloadDir.listFiles()) {
			String fileName = imageFile.getName();
			String idStr = fileName.split("\\.")[0];
			int id = Integer.valueOf(idStr);
			if (id > maxId) {
				maxId = id;
			}
		}
		imageId = new AtomicInteger(maxId);
	}

}