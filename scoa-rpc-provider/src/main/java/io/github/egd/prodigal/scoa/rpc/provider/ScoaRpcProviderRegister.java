package io.github.egd.prodigal.scoa.rpc.provider;

import io.github.egd.prodigal.scoa.rpc.annotations.ScoaRpcProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Set;

public class ScoaRpcProviderRegister implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, @NonNull BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(EnableScoaRpcProvider.class.getName(), true);
        if (annotationAttributes != null) {
            String[] basePackages = (String[]) annotationAttributes.get("basePackages");
            scan(basePackages, registry);
        }
    }

    private void scan(String[] basePackages, BeanDefinitionRegistry registry) {
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false);
        scanner.addIncludeFilter((metadataReader, metadataReaderFactory) -> {
            ClassMetadata classMetadata = metadataReader.getClassMetadata();
            boolean isRpcProvider = metadataReader.getAnnotationMetadata().hasAnnotation(ScoaRpcProvider.class.getName());
            return isRpcProvider && classMetadata.isConcrete() && classMetadata.isIndependent()
                    && classMetadata.hasSuperClass() && !classMetadata.isAbstract() && !classMetadata.isInterface();
        });
        scanner.setScopedProxyMode(ScopedProxyMode.TARGET_CLASS);

        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
            candidateComponents.forEach(candidateComponent -> {
                ScannedGenericBeanDefinition beanDefinition = (ScannedGenericBeanDefinition) candidateComponent;
                AnnotationMetadata metadata = beanDefinition.getMetadata();
                beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
                String[] interfaceNames = metadata.getInterfaceNames();
                for (String interfaceName : interfaceNames) {
                    registry.registerBeanDefinition(interfaceName, beanDefinition);
                }
            });
        }
    }
}
