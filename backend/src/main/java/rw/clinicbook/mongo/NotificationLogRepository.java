package rw.clinicbook.mongo;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {

    List<NotificationLog> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
}
