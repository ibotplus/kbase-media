package com.eastrobot.kbs.media.util.tesseract;

import com.eastrobot.kbs.media.model.Constants;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * TesseractUtil
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-05-03 10:58
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "convert", name = "image.ocr.default", havingValue = Constants.TESSERACT)
public class TesseractUtil {

    /**
     * 中文简体
     */
    private static final String CHI_SIM = "chi_sim";
    /**
     * 中文繁体
     */
    private static final String CHI_TRA = "chi_tra";
    /**
     * 英文
     */
    private static final String ENG = "eng";

    /**
     * 默认使用中文
     */
    private static final String DEFAULT_LANG = CHI_SIM;

    private static Tesseract tesseract;

    @Value("${convert.image.ocr.tesseract.datapath}")
    private String datapath;

    private static String languagePath;

    @PostConstruct
    private void init() {
        tesseract = new Tesseract();
        // 优先加载本地的设置 没有就加载环境变量 再没有就报错
        if (StringUtils.isBlank(datapath)) {
            datapath = System.getenv("TESSDATA_PREFIX");
            if (StringUtils.isBlank(datapath)) {
                log.error("initialize tesseract occurred exception. check language data path.");
                throw new IllegalArgumentException("initialize tesseract occurred exception. check language data path");
            }
        }

        languagePath = datapath;
        log.info("initialize tesseract ocr complete.");
    }

    /**
     * 默认使用中文去解析图片内容
     */
    public static String ocr(String imageFilePath) throws Exception {
        tesseract = new Tesseract();
        tesseract.setDatapath(languagePath);
        tesseract.setLanguage(DEFAULT_LANG);

        return tesseract.doOCR(new File(imageFilePath));
    }
}
