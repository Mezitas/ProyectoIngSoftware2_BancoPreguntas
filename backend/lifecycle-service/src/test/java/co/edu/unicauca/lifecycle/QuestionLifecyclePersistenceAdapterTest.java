package co.edu.unicauca.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.lifecycle.adapter.persistence.QuestionLifecyclePersistenceAdapter;
import co.edu.unicauca.lifecycle.domain.QuestionLifecycle;
import co.edu.unicauca.lifecycle.model.QuestionStateEntity;
import co.edu.unicauca.lifecycle.model.QuestionStatus;
import co.edu.unicauca.lifecycle.repository.QuestionStateRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuestionLifecyclePersistenceAdapterTest {

    @Mock
    private QuestionStateRepository repository;
    @InjectMocks
    private QuestionLifecyclePersistenceAdapter adapter;

    @Test
    void updatesTheManagedEntityInsteadOfInsertingASecondState() {
        QuestionStateEntity stored = new QuestionStateEntity(12L, QuestionStatus.DRAFT, 4L);
        when(repository.findById(12L)).thenReturn(Optional.of(stored));
        when(repository.save(same(stored))).thenReturn(stored);
        QuestionLifecycle aggregate = QuestionLifecycle.reconstitute(12L, QuestionStatus.PENDING_REVIEW,
                4L, 4L, null, stored.getChangedAt());

        QuestionLifecycle persisted = adapter.save(aggregate);

        assertEquals(QuestionStatus.PENDING_REVIEW, persisted.status());
        verify(repository).save(same(stored));
    }
}
