package com.safespend.web;
import com.safespend.domain.Alert;
import com.safespend.repo.AlertRepo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
  private final AlertRepo repo;
  public AlertController(AlertRepo r){ this.repo=r; }
  @GetMapping
  public List<Alert> list(@RequestParam(required=false) String userId){
    if (userId==null)
      return repo.findAll(PageRequest.of(0,100, Sort.by("createdAt").descending())).getContent();
    return repo.findTop100ByUserIdOrderByCreatedAtDesc(userId);
  }
}
