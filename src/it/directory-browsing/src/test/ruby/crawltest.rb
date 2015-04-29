require_relative 'crawl'
warn "fix load path"
c = Crawl.new
c.target = "build/crawl"
c.base_url = "http://localhost:8989/caching/maven/releases/rubygems"

c.load("rails")
c.load("rails/")
c.load("rails/maven-metadata.xml")
c.load("rails/maven-metadata.xml.sha1")
c.load("rails/3.0.9")
c.load("rails/3.0.9/")
c.load("rails/3.0.9/rails-3.0.9.gem.sha1")
c.load("rails/3.0.9/rails-3.0.9.pom")
c.load("rails/3.0.9/rails-3.0.9.pom.sha1")
