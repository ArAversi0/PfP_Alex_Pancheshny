package com.pfp.companion.content.foundation.persistence;

import com.pfp.companion.content.entity.LoreArticle;
import com.pfp.companion.content.entity.RuleBookArticle;
import com.pfp.companion.content.entity.RuleCategory;
import com.pfp.companion.content.mediator.ContentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaContentRepositoryAdapter implements ContentRepository {

    private final LoreArticleJpaRepository loreRepository;
    private final RuleCategoryJpaRepository categoryRepository;
    private final RuleBookArticleJpaRepository ruleBookRepository;
    private final ContentEditorJpaRepository editorRepository;

    public JpaContentRepositoryAdapter(LoreArticleJpaRepository loreRepository,
            RuleCategoryJpaRepository categoryRepository, RuleBookArticleJpaRepository ruleBookRepository,
            ContentEditorJpaRepository editorRepository) {
        this.loreRepository = loreRepository;
        this.categoryRepository = categoryRepository;
        this.ruleBookRepository = ruleBookRepository;
        this.editorRepository = editorRepository;
    }

    @Override
    @Transactional
    public LoreArticle saveLoreArticle(LoreArticle article) {
        LoreArticleJpaEntity target = article.id() == 0
                ? new LoreArticleJpaEntity()
                : loreRepository.findById(article.id()).orElseThrow();
        target.title = article.title();
        target.content = article.content();
        target.imageUrl = article.imageUrl();
        target.updatedBy = article.updatedBy() == null ? null : editorRepository.findByPublicId(article.updatedBy())
                .orElseThrow(() -> new IllegalArgumentException("unknown content editor"));
        target.updatedAt = article.updatedAt() == null ? Instant.now() : article.updatedAt();
        return toDomain(loreRepository.saveAndFlush(target));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LoreArticle> findLoreArticle(long id) {
        return loreRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoreArticle> findAllLoreArticles() {
        return loreRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public void deleteLoreArticle(long id) {
        loreRepository.deleteById(id);
        loreRepository.flush();
    }

    @Override
    @Transactional
    public RuleCategory saveRuleCategory(RuleCategory category) {
        RuleCategoryJpaEntity target = category.id() == 0
                ? new RuleCategoryJpaEntity()
                : categoryRepository.findById(category.id()).orElseThrow();
        target.name = category.name();
        target.description = category.description();
        RuleCategoryJpaEntity saved = categoryRepository.saveAndFlush(target);
        return new RuleCategory(saved.id, saved.name, saved.description);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleCategory> findAllRuleCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> new RuleCategory(category.id, category.name, category.description))
                .toList();
    }

    @Override
    @Transactional
    public void deleteRuleCategory(long id) {
        categoryRepository.deleteDirectById(id);
        categoryRepository.flush();
    }

    @Override
    @Transactional
    public RuleBookArticle saveRuleBookArticle(RuleBookArticle article) {
        RuleBookArticleJpaEntity target = article.id() == 0
                ? new RuleBookArticleJpaEntity()
                : ruleBookRepository.findById(article.id()).orElseThrow();
        target.category = categoryRepository.findById(article.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("unknown rule category"));
        target.title = article.title();
        target.content = article.content();
        RuleBookArticleJpaEntity saved = ruleBookRepository.saveAndFlush(target);
        return new RuleBookArticle(saved.id, saved.category.id, saved.title, saved.content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleBookArticle> findRuleBookArticlesByCategory(long categoryId) {
        return ruleBookRepository.findAllByCategoryIdOrderById(categoryId).stream()
                .map(article -> new RuleBookArticle(article.id, article.category.id, article.title, article.content))
                .toList();
    }

    private LoreArticle toDomain(LoreArticleJpaEntity source) {
        return new LoreArticle(source.id, source.title, source.content, source.imageUrl,
                source.updatedBy == null ? null : source.updatedBy.publicId, source.updatedAt);
    }
}
