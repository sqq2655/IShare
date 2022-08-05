package com.sqqone.code.service.impl;

import com.sqqone.code.entity.Link;
import com.sqqone.code.repository.LinkRepository;
import com.sqqone.code.service.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.util.List;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/6/28 10:59
 */
@Service
public class LinkServiceImpl implements LinkService {

    @Autowired
    private LinkRepository linkRepository;

    @Override
    public List<Link> list(Integer page, Integer pageSize, Sort.Direction direction, String... properties) {
        return linkRepository.findAll(PageRequest.of(page-1,pageSize,direction,properties)).getContent();
    }

    @Override
    public Long getCount() {
        return linkRepository.count();
    }

    @Override
    public void save(Link link) {
        linkRepository.save(link);
    }

    @Override
    public void delete(Integer id) {
        linkRepository.deleteById(id);
    }

    @Override
    public List<Link> listAll(Sort.Direction direction, String... properties) {
        Sort sort = Sort.by(direction,properties);
        return linkRepository.findAll(sort);
    }

    @Override
    public Link getById(Integer id) {
        return linkRepository.getOne(id);
    }
}
