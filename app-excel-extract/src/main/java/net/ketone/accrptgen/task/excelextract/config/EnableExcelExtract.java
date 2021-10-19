package net.ketone.accrptgen.task.excelextract.config;


import net.ketone.accrptgen.common.store.CloudStorageConfig;
import net.ketone.accrptgen.common.store.LocalStorageConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {ExcelExtractTaskConfiguration.class})
public @interface EnableExcelExtract {
}
