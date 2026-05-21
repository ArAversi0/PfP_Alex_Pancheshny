package com.pfp.companion.content.mediator;

import com.pfp.companion.content.entity.LoreArticle;
import com.pfp.companion.content.entity.RuleBookArticle;
import com.pfp.companion.content.entity.RuleCategory;
import java.util.List;
import java.util.Optional;

public interface ContentRepository {

    LoreArticle saveLoreArticle(LoreArticle article);

    Optional<LoreArticle> findLoreArticle(long id);

    List<LoreArticle> findAllLoreArticles();

    void deleteLoreArticle(long id);

    RuleCategory saveRuleCategory(RuleCategory category);

    List<RuleCategory> findAllRuleCategories();

    void deleteRuleCategory(long id);

    RuleBookArticle saveRuleBookArticle(RuleBookArticle article);

    List<RuleBookArticle> findRuleBookArticlesByCategory(long categoryId);
}

