# ridaizu-tool
ridaizu-tool
package jp.co.olc.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jp.co.olc.core.constant.OlcMessage;
import jp.co.olc.core.exception.OlcFrameWorkException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JacksonUtil {

    private JacksonUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final String XML_SCHEMA = "<?xml version=\"1.0\" encoding=";

    public static Map<String, Object> toMap(File file, Charset charset, boolean isXml) {
        try {
            return toMap(FileUtils.readFileToString(file, charset), isXml);
        } catch (IOException e) {
            throw new OlcFrameWorkException(e, OlcMessage.FWK_E_00027);
        }
    }

    public static Map<String, Object> toMap(String content, boolean isXml) {
        ObjectMapper objectMapper = getObjectMapper(isXml);
        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };
        try {
            return objectMapper.readValue(content, typeReference);
        } catch (IOException e) {
            if (isXml) {
                throw new OlcFrameWorkException(e, OlcMessage.FWK_E_00026);
            }
            throw new OlcFrameWorkException(e, OlcMessage.FWK_E_00013);
        }
    }

    public static <T> List<T> toList(File file, Charset charset, Class<T> clazz, boolean isXml) {
        try {
            return toList(FileUtils.readFileToString(file, charset), clazz, isXml);
        } catch (IOException e) {
            throw new OlcFrameWorkException(e, OlcMessage.FWK_E_00027);
        }
    }

    public static <T> List<T> toList(String content, Class<T> clazz, boolean isXml) {
        ObjectMapper objectMapper = getObjectMapper(isXml);
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, clazz);
        try {
            return objectMapper.readValue(content, javaType);
        } catch (IOException e) {
            if (isXml) {
                throw new OlcFrameWorkException(e, OlcMessage.FWK_E_00026);
            }
            throw new OlcFrameWorkException(e, OlcMessage.FWK_E_00013);
        }
    }

    public static <T> T toBean(File file, Charset charset, Class<T> clazz, boolean isXml) {
        try {
            return toBean(FileUtils.readFileToString(file, charset), clazz, isXml);
        } catch (IOException e) {
            throw new OlcFrameWorkException(e, OlcMessage.FWK_E_00027);
        }
    }

    public static <T> T toBean(String content, Class<T> clazz, boolean isXml) {
        ObjectMapper objectMapper = getObjectMapper(isXml);
        try {
            return objectMapper.readValue(content, clazz);
        } catch (IOException e) {
            if (isXml) {
                throw new OlcFrameWorkException(e, OlcMessage.FWK_E_00026);
            }
            throw new OlcFrameWorkException(e, OlcMessage.FWK_E_00013);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> convertToMap(Object fromValue) {
        ObjectMapper objectMapper = getObjectMapper(false);
        return objectMapper.convertValue(fromValue, Map.class);
    }

    public static <T> T convertFromMap(Map<String, Object> fromMap, Class<T> clazz) {
        ObjectMapper objectMapper = getObjectMapper(false);
        return objectMapper.convertValue(fromMap, clazz);
    }

    public static String toJsonString(Object object) {
        ObjectMapper objectMapper = getObjectMapper(false);
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new OlcFrameWorkException(e, OlcMessage.FWK_E_00013);
        }
    }

    public static String toJsonString(Object object, String dateFormatPattern) {
        return toJsonString(object, dateFormatPattern, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static String toJsonString(Object object, String dateFormatPattern, Locale locale) {
        ObjectMapper objectMapper = getObjectMapper(false);
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPattern, locale);
        try {
            return objectMapper.writer(dateFormat).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new OlcFrameWorkException(e, OlcMessage.FWK_E_00013);
        }
    }

    public static String toXmlString(Object object) {
        ObjectMapper objectMapper = getObjectMapper(true);
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new OlcFrameWorkException(e, OlcMessage.FWK_E_00026);
        }
    }

    public static String toXmlString(Object object, String dateFormatPattern) {
        return toXmlString(object, dateFormatPattern, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static String toXmlString(Object object, String dateFormatPattern, Locale locale) {
        ObjectMapper objectMapper = getObjectMapper(true);
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPattern, locale);
        try {
            return objectMapper.writer(dateFormat).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new OlcFrameWorkException(e, OlcMessage.FWK_E_00026);
        }
    }

    public static void toJsonFile(File jsonFile, Charset charset, Object object) {
        toJsonFile(jsonFile, charset, object, false);
    }

    public static void toJsonFile(File jsonFile, Charset charset, Object object, boolean addBOM) {
        try {
            String writeString = null;
            if (addBOM) {
                writeString = new String(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, charset);
            }
            writeString = writeString + toJsonString(object);
            FileUtils.write(jsonFile, writeString, charset);
        } catch (IOException e) {
            throw new OlcFrameWorkException(e, OlcMessage.FWK_E_00013);
        }
    }

    public static void toJsonFile(File jsonFile, Charset charset, Object object, String dateFormatPattern) {
        toJsonFile(jsonFile, charset, object, dateFormatPattern, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static void toJsonFile(File jsonFile, Charset charset, Object object, String dateFormatPattern, Locale locale) {
        toJsonFile(jsonFile, charset, object, dateFormatPattern, locale, false);
    }

    public static void toJsonFile(File jsonFile, Charset charset, Object object, String dateFormatPattern, Locale locale, boolean addBOM) {
        try {
            String writeString = null;
            if (addBOM) {
                writeString = new String(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, charset);
            }
            writeString = writeString + toJsonString(object, dateFormatPattern, locale);
            FileUtils.write(jsonFile, writeString, charset);
        } catch (IOException e) {
            throw new OlcFrameWorkException(e, OlcMessage.FWK_E_00013);
        }
    }

    public static void toXmlFile(File xmlFile, Charset charset, Object object) {
        try {
            String xmlString = XML_SCHEMA + "\"" + charset.toString() + "\"?>" + toXmlString(object);
            FileUtils.write(xmlFile, xmlString, charset);
        } catch (IOException e) {
            throw new OlcFrameWorkException(e, OlcMessage.FWK_E_00026);
        }
    }

    public static void toXmlFile(File xmlFile, Charset charset, Object object, String dateFormatPattern) {
        toXmlFile(xmlFile, charset, object, dateFormatPattern, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static void toXmlFile(File xmlFile, Charset charset, Object object, String dateFormatPattern, Locale locale) {
        try {
            String xmlString = XML_SCHEMA + "\"" + charset.toString() + "\"?>" + toXmlString(object, dateFormatPattern, locale);
            FileUtils.write(xmlFile, xmlString, charset);
        } catch (IOException e) {
            throw new OlcFrameWorkException(e, OlcMessage.FWK_E_00026);
        }
    }

    public static ObjectMapper getObjectMapper(boolean xml) {
        ObjectMapper objectMapper;
        if (xml) {
            objectMapper = new XmlMapper();
        } else {
            objectMapper = new ObjectMapper();
        }
        return objectMapper
                .findAndRegisterModules()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}

