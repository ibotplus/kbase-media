package com.eastrobot.converter.util.abbyy;

import com.abbyy.FREngine.*;
import com.eastrobot.converter.model.Constants;
import com.eastrobot.converter.model.FileType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.nio.charset.Charset;

/**
 * AbbyyUtil
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-18 16:54
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "convert", name = "image.ocr.default", havingValue = Constants.ABBYY)
public class AbbyyOcrUtil {

    @Value("${convert.image.ocr.abbyy.path}")
    private String path;

    @Value("${convert.image.ocr.abbyy.license}")
    private String license;

    private static IEngine engine;

    private static IPrepareImageMode imageMode;

    @PostConstruct
    private void init() {
        try {
            engine = Engine.GetEngineObject(path, license);
            engine.LoadPredefinedProfile(AbbyyConstants.TextExtraction_Accuracy);

            // 加载图像阶段参数设置
            imageMode = engine.CreatePrepareImageMode();
            imageMode.setCorrectSkew(true); // 纠正倾斜
            imageMode.setCorrectSkewMode(CorrectSkewModeEnum.CSM_CorrectSkewByHorizontalText.getValue()
                    | CorrectSkewModeEnum.CSM_CorrectSkewByVerticalText.getValue()); // 水平文本|垂直文本
            // imageMode.setDiscardColorImage(true);   //5丢弃色彩
            imageMode.setEnhanceLocalContrast(true); // 增强局部对比度
            imageMode.setCreatePreview(false);
        } catch (Exception e) {
            log.error("initialize abbyy Engine occurred exception.", e);
        }
    }

    /**
     * 传入图片文件 解析返回结果 abbyy 会将结果写入文件
     * @param imageFilePath 图片绝对路径
     *
     * @author Yogurt_lei
     * @date 2018-04-18 17:32
     */
    public static String ocr(String imageFilePath) throws Exception {
        String outputFile = FilenameUtils.getFullPath(imageFilePath)
                + FilenameUtils.getBaseName(imageFilePath) +
                FileType.TXT.getExtensionWithPoint();
        IFRDocument document = engine.CreateFRDocumentFromImage(imageFilePath, imageMode);
        try {
            // 识别器参数
            IRecognizerParams iRecognizerParams = engine.CreateRecognizerParams();
            iRecognizerParams.setExactConfidenceCalculation(true);    //字符和单词的置信度将被更准确地定义，但是识别速度可能会变慢。
            // iRecognizerParams.setLowResolutionMode(true);               // 1指定低分辨率图像上的文本是否被识别
            iRecognizerParams.SetPredefinedTextLanguage("ChinesePRC, English"); // 单独效果会好点
            // iRecognizerParams.setBalancedMode(true);
            // iRecognizerParams.setProhibitInterblockHyphenation(true);
            iRecognizerParams.setProhibitItalic(true);
            iRecognizerParams.setProhibitSubscript(true);
            iRecognizerParams.setProhibitSuperscript(true);

            // 对象提取参数
            // IObjectsExtractionParams iObjectsExtractionParams = engine.CreateObjectsExtractionParams();
            // iObjectsExtractionParams.setEnableAggressiveTextExtraction(true);   //尽可能尝试提取更多的文本
            // iObjectsExtractionParams.setRemoveGarbage(true);   //4去除小于一定尺寸的多余的点
            // iObjectsExtractionParams.setRemoveTexture(true);   //3去除背景噪点

            // 页面处理参数
            IPageProcessingParams iPageProcessingParams = engine.CreatePageProcessingParams();
            // iPageProcessingParams.setProhibitColorObjectsAtProcessing(true);    //2滤除图像上的颜色对象
            iPageProcessingParams.setRecognizerParams(iRecognizerParams);
            // iPageProcessingParams.setObjectsExtractionParams(iObjectsExtractionParams);

            // 文档处理参数
            IDocumentProcessingParams iDocumentProcessingParams = engine.CreateDocumentProcessingParams();
            iDocumentProcessingParams.setPageProcessingParams(iPageProcessingParams);
            document.Process(iDocumentProcessingParams);

            // 设置导出参数 保存为UTF-8的Txt
            ITextExportParams textParams = engine.CreateTextExportParams();
            textParams.setAppendToEnd(false);
            textParams.setExportFormat(TXTExportFormatEnum.TEF_TXT);
            textParams.setExportParagraphsAsOneLine(false);
            textParams.setWriteBomCharacter(false);
            textParams.setWriteRunningTitles(false);
            document.Export(outputFile, FileExportFormatEnum.FEF_TextVersion10Defaults, textParams);
        } finally {
            document.Close();
        }

        String resultText = FileUtils.readFileToString(new File(outputFile), Charset.forName("utf-8"));
        return resultText.trim();
    }

    @PreDestroy
    private void destroy() {
        try {
            Engine.DeinitializeEngine();
        } catch (Exception e) {
            log.error("destroy abbyy Engine occurred exception.", e);
        }
    }
}
